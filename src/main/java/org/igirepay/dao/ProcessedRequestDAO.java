package org.igirepay.dao;

import org.igirepay.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class ProcessedRequestDAO {


    public boolean isProcessed(String referenceId) {
        String sql = "SELECT 1 FROM processed_requests WHERE reference_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);) {

            stmt.setString(1, referenceId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.out.println("Could not check reference: " + e.getMessage());
            return false;
        }
    }

    public boolean markAsProcessed(Connection conn, String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?) "
                + "ON CONFLICT (reference_id) DO NOTHING";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, referenceId);
            return stmt.executeUpdate() == 1;   //
        }
    }


    public boolean markAsProcessed(String referenceId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return markAsProcessed(conn, referenceId);
        } catch (SQLException e) {
            System.out.println("Could not save reference: " + e.getMessage());
            return false;
        }
    }


    public void viewAll() {
        String sql = "SELECT reference_id, processed_at FROM processed_requests ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("--- Processed requests ---");
            boolean foundAny = false;
            while (rs.next()) {
                foundAny = true;
                System.out.println(rs.getString("reference_id")
                        + " | " + rs.getTimestamp("processed_at"));
            }
            if (!foundAny) {
                System.out.println("(none yet)");
            }

        } catch (SQLException e) {
            System.out.println("Could not read processed requests: " + e.getMessage());
        }
    }
}
