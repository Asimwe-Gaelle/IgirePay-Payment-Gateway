package org.igirepay.dao;

import org.igirepay.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// This DAO talks to the "processed_requests" table.
// It remembers every reference ID we have handled, so a retried request
// (same reference ID) can be recognised and rejected.
public class ProcessedRequestDAO {

    // READ: has this reference ID been processed before?  true = yes, false = no
    public boolean isProcessed(String referenceId) {
        String sql = "SELECT 1 FROM processed_requests WHERE reference_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);) {

            stmt.setString(1, referenceId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();   // a row was found = already processed
            }

        } catch (SQLException e) {
            System.out.println("Could not check reference: " + e.getMessage());
            return false;
        }
    }

    // CREATE (used inside a bigger database transaction).
    // It uses the Connection that AccountDAO gives us, so this insert is saved
    // or undone TOGETHER with the balance changes.
    //
    // "ON CONFLICT DO NOTHING" means: if the reference already exists, don't crash,
    // just insert nothing.
    // Returns true if the reference was NEW, false if it was a DUPLICATE.
    public boolean markAsProcessed(Connection conn, String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?) "
                + "ON CONFLICT (reference_id) DO NOTHING";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, referenceId);
            return stmt.executeUpdate() == 1;   // 1 row inserted = new, 0 rows = duplicate
        }
    }

    // CREATE (on its own): same as above, but opens its own connection.
    public boolean markAsProcessed(String referenceId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return markAsProcessed(conn, referenceId);
        } catch (SQLException e) {
            System.out.println("Could not save reference: " + e.getMessage());
            return false;
        }
    }

    // READ: print every processed reference ID
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
