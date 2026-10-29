package org.igirepay.dao;

import org.igirepay.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class AccountDAO {
    public int createAccount(int customerId, String accountType, double startingBalance) {
        String sql = "INSERT INTO accounts (customer_id, account_type, balance) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, customerId);
            stmt.setString(2, accountType);
            stmt.setDouble(3, startingBalance);
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


    public double getBalance(int accountId) {
        String sql = "SELECT balance FROM accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not read balance: " + e.getMessage());
        }
        return -1;
    }


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



    public void deposit(int accountId, double amount, String referenceId) {
        if (amount <= 0) {
            System.out.println("Deposit failed: amount must be more than 0");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);   // start the database transaction
            try {

                if (!markReferenceAsProcessed(conn, referenceId)) {
                    System.out.println("DUPLICATE: reference " + referenceId + " was already processed");
                    conn.rollback();
                    return;
                }

                if (!addToBalance(conn, accountId, amount)) {
                    System.out.println("Deposit failed: account " + accountId + " not found");
                    conn.rollback();
                    return;
                }

                saveTransaction(conn, accountId, referenceId, "DEPOSIT", amount);

                conn.commit();   //
                System.out.println("Deposit of " + amount + " into account " + accountId + " done");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }


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
                    conn.rollback();
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
                if (!subtractFromBalance(conn, fromAccountId, amount)) {
                    System.out.println("Send failed: sender account " + fromAccountId
                            + " not found or not enough money");
                    conn.rollback();
                    return;
                }

                if (!addToBalance(conn, toAccountId, amount)) {
                    System.out.println("Send failed: receiver account " + toAccountId + " not found");
                    conn.rollback();   // puts the sender's money back
                    return;
                }

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

    private boolean markReferenceAsProcessed(Connection conn, String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?) "
                + "ON CONFLICT (reference_id) DO NOTHING";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, referenceId);
            return stmt.executeUpdate() == 1;
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


    private boolean subtractFromBalance(Connection conn, int accountId, double amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, accountId);
            stmt.setDouble(3, amount);
            return stmt.executeUpdate() == 1;
        }
    }


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
