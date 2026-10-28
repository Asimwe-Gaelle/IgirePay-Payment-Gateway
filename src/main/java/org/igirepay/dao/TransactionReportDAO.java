
package org.igirepay.dao;

import org.igirepay.db.DatabaseConnection;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

public class TransactionReportDAO {

    // FEATURE 1: Export all transactions to a CSV file.
    public void exportToCSV(String fileName)
            throws SQLException, IOException {

        String sql = "SELECT * FROM transactions";

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery();
             BufferedWriter writer =
                     new BufferedWriter(new FileWriter(fileName))) {

            // Write the headings into the CSV file.
            writer.write(
                    "Transaction ID,Account ID,Type,Amount,Date,Reference ID"
            );
            writer.newLine();

            // Read transactions one by one.
            while (result.next()) {

                writer.write(
                        result.getString("transaction_id") + "," +
                                result.getString("account_id") + "," +
                                result.getString("transaction_type") + "," +
                                result.getString("amount") + "," +
                                result.getString("transaction_date") + "," +
                                result.getString("reference_id")
                );

                writer.newLine();
            }
        }

        System.out.println("CSV file created: " + fileName);
    }

    // FEATURE 2: Display the summary for one day.
    public void showDailySummary(Date selectedDate)
            throws SQLException {

        String sql =
                "SELECT COUNT(*) AS number_of_transactions, " +
                        "COALESCE(SUM(amount), 0) AS total_amount " +
                        "FROM transactions " +
                        "WHERE transaction_date >= ? " +
                        "AND transaction_date < ?";

        LocalDate date = selectedDate.toLocalDate();

        Timestamp startOfDay =
                Timestamp.valueOf(date.atStartOfDay());

        Timestamp startOfNextDay =
                Timestamp.valueOf(date.plusDays(1).atStartOfDay());

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Choose the start and end of the day.
            statement.setTimestamp(1, startOfDay);
            statement.setTimestamp(2, startOfNextDay);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    System.out.println("\n=== Daily Summary ===");
                    System.out.println("Date: " + selectedDate);

                    System.out.println(
                            "Number of transactions: " +
                                    result.getInt("number_of_transactions")
                    );

                    System.out.println(
                            "Total transaction amount: " +
                                    result.getBigDecimal("total_amount") +
                                    " RWF"
                    );
                }
            }
        }
    }

    // FEATURE 3: Display transactions belonging to a customer.
    public void showCustomerStatement(int customerId)
            throws SQLException {

        String sql =
                "SELECT t.* FROM transactions t " +
                        "JOIN accounts a ON t.account_id = a.account_id " +
                        "WHERE a.customer_id = ? " +
                        "ORDER BY t.transaction_date DESC";

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Find transactions for this customer.
            statement.setInt(1, customerId);

            try (ResultSet result = statement.executeQuery()) {

                System.out.println("\n=== Customer Statement ===");
                System.out.println("Customer ID: " + customerId);

                boolean found = false;

                while (result.next()) {
                    found = true;

                    System.out.println("------------------------");
                    System.out.println(
                            "Transaction ID: " +
                                    result.getObject("transaction_id")
                    );
                    System.out.println(
                            "Account ID: " +
                                    result.getObject("account_id")
                    );
                    System.out.println(
                            "Type: " +
                                    result.getString("transaction_type")
                    );
                    System.out.println(
                            "Amount: " +
                                    result.getBigDecimal("amount") + " RWF"
                    );
                    System.out.println(
                            "Date: " +
                                    result.getObject("transaction_date")
                    );
                    System.out.println(
                            "Reference ID: " +
                                    result.getString("reference_id")
                    );
                }

                if (!found) {
                    System.out.println(
                            "No transactions found for this customer."
                    );
                }
            }
        }
    }
}
