package org.igirepay.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// This class has ONE job: give us a connection to the PostgreSQL database.
// Every DAO class calls DatabaseConnection.getConnection() when it needs the database.
public class DatabaseConnection {

    // Change these 3 lines to match YOUR PostgreSQL setup
    private static final String URL = "jdbc:postgresql://localhost:5432/igirepay_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "abcd";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connected to PostgreSQL successfully!");
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
