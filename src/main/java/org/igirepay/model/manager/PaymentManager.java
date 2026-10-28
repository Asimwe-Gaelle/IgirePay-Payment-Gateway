package org.igirepay.model.manager;

import org.igirepay.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// PaymentManager keeps all the data in memory using Java Collections
// and makes sure the same payment is never processed twice.
public class PaymentManager {

    // MAP: find a customer quickly by customerId        (key = id, value = Customer)
    private Map<Integer, Customer> customers = new HashMap<>();

    // MAP + LIST: each customer can have MANY accounts   (key = customerId, value = list of accounts)
    private Map<Integer, List<Account>> accountsByCustomer = new HashMap<>();

    // MAP: find an account quickly by its account number (key = number, value = Account)
    private Map<String, Account> accountsByNumber = new HashMap<>();

    // LIST: keeps every successful transaction, in order
    private List<Transaction> transactionHistory = new ArrayList<>();

    // SET: keeps reference IDs that were already processed.
    // A Set does NOT allow duplicates, and contains() is very fast.
    private Set<String> processedReferenceIds = new HashSet<>();

    // LIST: keeps a text message for every failed transaction
    private List<String> failedTransactionLog = new ArrayList<>();

    // ---------- Managing customers and accounts ----------

    public void addCustomer(Customer customer) {
        customers.put(customer.getCustomerId(), customer);
        // Start an empty list of accounts for this customer
        accountsByCustomer.put(customer.getCustomerId(), new ArrayList<>());
    }

    public void addAccount(int customerId, Account account) {
        List<Account> list = accountsByCustomer.get(customerId);
        if (list == null) {
            System.out.println("Cannot add account: customer " + customerId + " not found");
            return;
        }
        list.add(account);
        accountsByNumber.put(account.getAccountNumber(), account);
    }

    public Account findAccount(String accountNumber) {
        return accountsByNumber.get(accountNumber);   // returns null if not found
    }

    public List<Account> getAccountsOfCustomer(int customerId) {
        return accountsByCustomer.get(customerId);
    }

    // ---------- Processing a payment ----------

    public void processPayment(String accountNumber, Transaction transaction) {
        String ref = transaction.getReferenceId();

        // Step 1: does the account exist?
        Account account = findAccount(accountNumber);
        if (account == null) {
            logFailure(ref, "Account " + accountNumber + " not found");
            return;
        }

        // Step 2: DUPLICATE CHECK. Was this reference ID already processed?
        if (processedReferenceIds.contains(ref)) {
            logFailure(ref, "DUPLICATE transaction, reference already processed");
            return;   // stop here, so the customer is NOT charged again
        }

        // Step 3: process it. The methods are void, so we compare the
        // balance before and after to see if the transaction worked.
        double balanceBefore = account.getBalance();
        account.processTransaction(transaction);
        double balanceAfter = account.getBalance();

        if (balanceAfter != balanceBefore) {
            // Success: remember the reference ID and save the transaction
            processedReferenceIds.add(ref);
            transactionHistory.add(transaction);
        } else {
            // Nothing changed, so it failed (the account already printed why)
            logFailure(ref, transaction.getTransactionType() + " of "
                    + transaction.getAmount() + " failed on account " + accountNumber);
        }
    }

    // Saves a message in the failed log and prints it
    private void logFailure(String referenceId, String reason) {
        String message = LocalDateTime.now() + " | " + referenceId + " | " + reason;
        failedTransactionLog.add(message);
        System.out.println("FAILED: " + message);
    }

    // ---------- Showing the data ----------

    public void printTransactionHistory() {
        System.out.println("--- Transaction history (" + transactionHistory.size() + ") ---");
        for (Transaction t : transactionHistory) {
            System.out.println(t);
        }
    }

    public void printFailedLog() {
        System.out.println("--- Failed transactions (" + failedTransactionLog.size() + ") ---");
        for (String message : failedTransactionLog) {
            System.out.println(message);
        }
    }

    public void printProcessedReferenceIds() {
        System.out.println("--- Processed reference IDs ---");
        System.out.println(processedReferenceIds);
    }

    public void printCustomerAccounts(int customerId) {
        Customer customer = customers.get(customerId);
        List<Account> list = accountsByCustomer.get(customerId);
        if (customer == null) {
            System.out.println("Customer " + customerId + " not found");
            return;
        }
        System.out.println("--- Accounts of " + customer.getFullName() + " ---");
        for (Account a : list) {
            System.out.println(a);
        }
    }

    // Getters so other classes can read the collections if needed
    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public Set<String> getProcessedReferenceIds() {
        return processedReferenceIds;
    }

    public List<String> getFailedTransactionLog() {
        return failedTransactionLog;
    }
}
