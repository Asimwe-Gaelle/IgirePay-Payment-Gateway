package org.igirepay.service;

import org.igirepay.dao.*;
import org.igirepay.model.Customer;
import org.igirepay.dao.TransactionReportDAO;
import java.sql.Date;
import java.sql.SQLException;
import java.io.IOException;

public class TransactionService {
    private static final double SAVINGS_MAX_WITHDRAWAL = 1000000;
    private static final double SAVINGS_WITHDRAWAL_FEE = 5000.0;
    private final TransactionReportDAO reportDAO = new TransactionReportDAO();

    private CustomerDAO customerDAO = new CustomerDAO();
    private AccountDAO accountDAO = new AccountDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();
    private ProcessedRequestDAO processedRequestDAO = new ProcessedRequestDAO();

    // ----------- Customers -------

    public void registerCustomer(String name, String email, String phone) {
        try {
            if (name.isEmpty() || !email.contains("@") || !phone.matches("[0-9]{10}")) {
                throw new IllegalArgumentException("Register failed: invalid name, email or phone format");
            }
            customerDAO.addCustomer(new Customer(0, name, phone, email));
            System.out.println("Customer registered successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void updateCustomer(int id, String name, String email, String phone) {
        try {
            Customer customer = customerDAO.findCustomerById(id);
            if (customer == null) {
                throw new IllegalArgumentException("Update failed: no customer with id " + id);
            }
            if (!email.isEmpty() && !email.contains("@")) {
                throw new IllegalArgumentException("Update failed: invalid email format");
            }
            if (!phone.isEmpty() && !phone.matches("[0-9]{10}")) {
                throw new IllegalArgumentException("Update failed: phone must be 10 digits");
            }

            if (!name.isEmpty()) customer.setFullName(name);
            if (!email.isEmpty()) customer.setEmail(email);
            if (!phone.isEmpty()) customer.setPhoneNumber(phone);

            customerDAO.updateCustomer(customer);
            System.out.println("Customer updated successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------- Accounts ----------

    private void createAccount(int customerId, String type, double balance) {
        try {
            if (balance < 0) throw new IllegalArgumentException("Balance must be non-negative");
            if (customerDAO.findCustomerById(customerId) == null) {
                throw new IllegalArgumentException("Invalid customer ID");
            }
            accountDAO.createAccount(customerId, type, balance);
            System.out.println(type + " account created!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void createWalletAccount(int customerId, double balance) {
        createAccount(customerId, "WALLET", balance);
    }

    public void createSavingsAccount(int customerId, double balance) {
        createAccount(customerId, "SAVINGS", balance);
    }

    // ---------- Transactions ----------

    public void deposit(int accountId, String refId, double amount) {
        try {
            if (amount <= 0) throw new IllegalArgumentException("Deposit failed: amount must be > 0");
            if (processedRequestDAO.isProcessed(refId)) {
                throw new IllegalStateException("Duplicate transaction detected!");
            }
            if (accountDAO.getAccountType(accountId) == null) {
                throw new IllegalArgumentException("Invalid account ID");
            }

            accountDAO.deposit(accountId, amount, refId);
            System.out.println("Deposit successful!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void withdraw(int accountId, String refId, double amount) {
        try {
            if (amount <= 0) throw new IllegalArgumentException("Withdraw failed: amount must be > 0");
            if (processedRequestDAO.isProcessed(refId)) {
                throw new IllegalStateException("Duplicate transaction detected!");
            }
            if (accountDAO.getAccountType(accountId) == null) {
                throw new IllegalArgumentException("Invalid account ID");
            }

            if ("SAVINGS".equals(accountDAO.getAccountType(accountId))) {
                if (amount > SAVINGS_MAX_WITHDRAWAL) {
                    throw new IllegalArgumentException("Savings withdrawal limit exceeded");
                }
                System.out.println("Note: a savings fee of " + SAVINGS_WITHDRAWAL_FEE + " is added");
                amount += SAVINGS_WITHDRAWAL_FEE;
            }


            accountDAO.withdraw(accountId, amount, refId);
            System.out.println("Withdrawal successful!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void transfer(int fromAccountId, int toAccountId, String refId, double amount) {
        try {
            if (amount <= 0) throw new IllegalArgumentException("Transfer failed: amount must be > 0");
            if (processedRequestDAO.isProcessed(refId)) {
                throw new IllegalStateException("Duplicate transaction detected!");
            }
            if (accountDAO.getAccountType(fromAccountId) == null || accountDAO.getAccountType(toAccountId) == null) {
                throw new IllegalArgumentException("Invalid account ID(s)");
            }
            if ("SAVINGS".equals(accountDAO.getAccountType(fromAccountId))) {
                throw new IllegalArgumentException("Savings accounts cannot send money");
            }

            accountDAO.sendMoney(fromAccountId, toAccountId, amount, refId);
            System.out.println("Transfer successful!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewTransactions(int accountId) {
        try {
            if (accountDAO.getAccountType(accountId) == null) {
                throw new IllegalArgumentException("Account " + accountId + " not found");
            }
            transactionDAO.viewTransactionHistory(accountId);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public String generateReferenceId() {
        return "REF-" + System.currentTimeMillis();
    }


    public void exportTransactions(String fileName)
            throws SQLException, IOException {

        reportDAO.exportToCSV(fileName);
    }

    public void showDailySummary(Date date)
            throws SQLException {

        reportDAO.showDailySummary(date);
    }

    public void showCustomerStatement(int customerId)
            throws SQLException {

        if (customerId <= 0) {
            throw new IllegalArgumentException(
                    "Customer ID must be greater than zero."
            );
        }

        reportDAO.showCustomerStatement(customerId);
    }




}
