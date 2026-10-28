package org.igirepay.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.igirepay.db.DatabaseConnection;
import org.igirepay.model.Customer;


// DAO = Data Access Object.
// This class talks to the "customers" table so the rest of the program doesn't have to.
public class CustomerDAO {

    // CREATE: add a new customer to the database
    public void addCustomer(Customer customer) {

        // The ? marks are placeholders. We fill them in below.
        // (Never join user text into the SQL yourself. Placeholders keep it safe.)
        String sql = "INSERT INTO customers (full_name, email, phone_number) VALUES (?, ?, ?)";

        // try-with-resources: the connection and statement are closed automatically
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getFullName());     // fills the 1st ?
            stmt.setString(2, customer.getEmail());        // fills the 2nd ?
            stmt.setString(3, customer.getPhoneNumber());  // fills the 3rd ?
            stmt.executeUpdate();                          // run the INSERT

            // The database created the id for us. Read it and store it in the object.
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

    // UPDATE: change the details of an existing customer
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

    // READ: find one customer by id. Returns null if there is no such customer.
    public Customer findCustomerById(int customerId) {
        String sql = "SELECT id, full_name, email, phone_number FROM customers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Build a Customer object from the row we found
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
        return null;   // not found (or an error happened)
    }

    // READ: print all the accounts that belong to one customer
    public void viewAccountsOfCustomer(int customerId) {
        String sql = "SELECT id, account_type, balance, created_at "
                + "FROM accounts WHERE customer_id = ? ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                boolean foundAny = false;
                while (rs.next()) {   // one loop for every account
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