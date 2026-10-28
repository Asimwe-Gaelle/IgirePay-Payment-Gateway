package org.igirepay.dao;

import org.igirepay.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// This DAO talks to the "accounts" table.
// Deposit, withdraw and send also write to "transactions" and "processed_requests".
//
// IMPORTANT IDEA: a DATABASE TRANSACTION.
// For money, we need "all or nothing". We turn off auto-commit, do all the steps,
// then call commit() to save them together. If anything goes wrong we call
// rollback(), which undoes every step. So we never lose or invent money.
public class AccountDAO {

    // CREATE: open a new account for a customer. Returns the new account id (or -1 if it failed).
    public int createAccount(int customerId, String accountType, double startingBalance) {
        String sql = "INSERT INTO accounts (customer_id, account_type, balance) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, customerId);
            stmt.setString(2, accountType);      // "WALLET" or "SAVINGS"
            stmt.setDouble(3, startingBalance);  // (real banks use BigDecimal for money, double keeps this simple)
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt("id");
                    System.out.println(accountType + " account created with id " + newId);
                    return newId;
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not create account: " + e.getMessage());
        }
        return -1;
    }

    // READ: get the balance of an account (returns -1 if the account is not found)
    public double getBalance(int accountId) {
        String sql = "SELECT balance FROM accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {   // executeQuery is used for SELECT
                if (rs.next()) {                         // moves to the first row, if there is one
                    return rs.getDouble("balance");
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not read balance: " + e.getMessage());
        }
        return -1;
    }

    // READ: get the type of an account ("WALLET" or "SAVINGS").
    // Returns null if the account is not found.
    public String getAccountType(int accountId) {
        String sql = "SELECT account_type FROM accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("account_type");
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not read account type: " + e.getMessage());
        }
        return null;
    }


    // UPDATE: deposit money
    public void deposit(int accountId, double amount, String referenceId) {
        if (amount <= 0) {
            System.out.println("Deposit failed: amount must be more than 0");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);   // start the database transaction
            try {
                // Step 1: duplicate check
                if (!markReferenceAsProcessed(conn, referenceId)) {
                    System.out.println("DUPLICATE: reference " + referenceId + " was already processed");
                    conn.rollback();
                    return;
                }
                // Step 2: add the money
                if (!addToBalance(conn, accountId, amount)) {
                    System.out.println("Deposit failed: account " + accountId + " not found");
                    conn.rollback();
                    return;
                }
                // Step 3: save the transaction record
                saveTransaction(conn, accountId, referenceId, "DEPOSIT", amount);

                conn.commit();   // save everything together
                System.out.println("Deposit of " + amount + " into account " + accountId + " done");

            } catch (SQLException e) {
                conn.rollback();   // something went wrong: undo everything
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    // UPDATE: withdraw money
    public void withdraw(int accountId, double amount, String referenceId) {
        if (amount <= 0) {
            System.out.println("Withdraw failed: amount must be more than 0");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!markReferenceAsProcessed(conn, referenceId)) {
                    System.out.println("DUPLICATE: reference " + referenceId + " was already processed");
                    conn.rollback();
                    return;
                }
                if (!subtractFromBalance(conn, accountId, amount)) {
                    System.out.println("Withdraw failed: account " + accountId
                            + " not found or not enough money");
                    conn.rollback();   // also undoes the reference check, so the customer can retry
                    return;
                }
                saveTransaction(conn, accountId, referenceId, "WITHDRAW", amount);

                conn.commit();
                System.out.println("Withdrawal of " + amount + " from account " + accountId + " done");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Withdraw failed: " + e.getMessage());
        }
    }

    // UPDATE: send money from one account to another.
    // Both balances change inside ONE database transaction, so either both change or neither.
    public void sendMoney(int fromAccountId, int toAccountId, double amount, String referenceId) {
        if (amount <= 0) {
            System.out.println("Send failed: amount must be more than 0");
            return;
        }
        if (fromAccountId == toAccountId) {
            System.out.println("Send failed: you cannot send money to the same account");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!markReferenceAsProcessed(conn, referenceId)) {
                    System.out.println("DUPLICATE: reference " + referenceId + " was already processed");
                    conn.rollback();
                    return;
                }
                // Take the money from the sender
                if (!subtractFromBalance(conn, fromAccountId, amount)) {
                    System.out.println("Send failed: sender account " + fromAccountId
                            + " not found or not enough money");
                    conn.rollback();
                    return;
                }
                // Give the money to the receiver
                if (!addToBalance(conn, toAccountId, amount)) {
                    System.out.println("Send failed: receiver account " + toAccountId + " not found");
                    conn.rollback();   // puts the sender's money back
                    return;
                }
                // reference_id must be unique in the table, so the receiver's record gets "-IN" added
                saveTransaction(conn, fromAccountId, referenceId, "SEND", amount);
                saveTransaction(conn, toAccountId, referenceId + "-IN", "DEPOSIT", amount);

                conn.commit();
                System.out.println("Sent " + amount + " from account " + fromAccountId
                        + " to account " + toAccountId);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Send failed: " + e.getMessage());
        }
    }

    // DELETE: remove inactive accounts.
    // Our table has no "status" column, so we define INACTIVE as:
    //   balance is 0  AND  the account has never had a transaction.
    // (Accounts with history are kept, so the records stay in the database.)
    public void deleteInactiveAccounts() {
        String sql = "DELETE FROM accounts "
                + "WHERE balance = 0 "
                + "AND id NOT IN (SELECT account_id FROM transactions)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int deleted = stmt.executeUpdate();
            System.out.println(deleted + " inactive account(s) deleted");

        } catch (SQLException e) {
            System.out.println("Could not delete inactive accounts: " + e.getMessage());
        }
    }

    // ---------- Small helper methods (only used inside this class) ----------

    // Tries to save the reference ID in processed_requests.
    // "ON CONFLICT DO NOTHING" means: if the reference already exists, don't crash, just insert nothing.
    // Returns true if it was NEW, false if it was a DUPLICATE.
    private boolean markReferenceAsProcessed(Connection conn, String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?) "
                + "ON CONFLICT (reference_id) DO NOTHING";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, referenceId);
            return stmt.executeUpdate() == 1;   // 1 row inserted = new, 0 rows = duplicate
        }
    }

    // Adds money to an account. Returns false if the account does not exist.
    private boolean addToBalance(Connection conn, int accountId, double amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, accountId);
            return stmt.executeUpdate() == 1;
        }
    }

    // Takes money from an account. Returns false if the account does not exist
    // OR if it does not have enough money ("balance >= ?" protects us).
    private boolean subtractFromBalance(Connection conn, int accountId, double amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, accountId);
            stmt.setDouble(3, amount);
            return stmt.executeUpdate() == 1;
        }
    }

    // Saves one row in the transactions table
    private void saveTransaction(Connection conn, int accountId, String referenceId,
                                 String type, double amount) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, reference_id, transaction_type, amount) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setString(2, referenceId);
            stmt.setString(3, type);
            stmt.setDouble(4, amount);
            stmt.executeUpdate();
        }
    }
}
