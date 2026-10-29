package org.igirepay.dao;

import org.igirepay.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class TransactionDAO {


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
                while (rs.next()) {
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
