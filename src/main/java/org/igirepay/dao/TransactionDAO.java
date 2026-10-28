package org.igirepay.dao;

import org.igirepay.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// This DAO reads from the "transactions" table.
public class TransactionDAO {

    // READ: print the transaction history of one account, newest first
    public void viewTransactionHistory(int accountId) {
        String sql = "SELECT reference_id, transaction_type, amount, created_at "
                + "FROM transactions WHERE account_id = ? "
                + "ORDER BY created_at DESC, id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("--- Transaction history for account " + accountId + " ---");

                boolean foundAny = false;
                while (rs.next()) {   // loop once for every row in the result
                    foundAny = true;
                    System.out.println(rs.getString("reference_id")
                            + " | " + rs.getString("transaction_type")
                            + " | " + rs.getDouble("amount")
                            + " | " + rs.getTimestamp("created_at"));
                }
                if (!foundAny) {
                    System.out.println("(no transactions yet)");
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not read transaction history: " + e.getMessage());
        }
    }
}
