package org.igirepay.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.igirepay.db.DatabaseConnection;
import org.igirepay.model.Customer;



public class CustomerDAO {
    public void addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (full_name, email, phone_number) VALUES (?, ?, ?)";


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhoneNumber());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setCustomerId(keys.getInt("id"));
                }
            }
            System.out.println("Customer added with id " + customer.getCustomerId());

        } catch (SQLException e) {
            System.out.println("Could not add customer: " + e.getMessage());
        }
    }


    public void updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhoneNumber());
            stmt.setInt(4, customer.getCustomerId());

            int rowsChanged = stmt.executeUpdate();   // how many rows were updated
            if (rowsChanged == 1) {
                System.out.println("Customer " + customer.getCustomerId() + " updated");
            } else {
                System.out.println("Update failed: customer " + customer.getCustomerId() + " not found");
            }

        } catch (SQLException e) {
            System.out.println("Could not update customer: " + e.getMessage());
        }
    }


    public Customer findCustomerById(int customerId) {
        String sql = "SELECT id, full_name, email, phone_number FROM customers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    return new Customer(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("phone_number"),
                            rs.getString("email"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not find customer: " + e.getMessage());
        }
        return null;
    }


    public void viewAccountsOfCustomer(int customerId) {
        String sql = "SELECT id, account_type, balance, created_at "
                + "FROM accounts WHERE customer_id = ? ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                boolean foundAny = false;
                while (rs.next()) {
                    foundAny = true;
                    System.out.println("  Account " + rs.getInt("id")
                            + " | " + rs.getString("account_type")
                            + " | balance: " + rs.getDouble("balance")
                            + " | opened: " + rs.getTimestamp("created_at"));
                }
                if (!foundAny) {
                    System.out.println("  (this customer has no accounts yet)");
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not read accounts: " + e.getMessage());
        }
    }


}