package org.igirepay.model.manager;

import org.igirepay.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PaymentManager {
    private Map<Integer, Customer> customers = new HashMap<>();
    private Map<Integer, List<Account>> accountsByCustomer = new HashMap<>();
    private Map<String, Account> accountsByNumber = new HashMap<>();
    private List<Transaction> transactionHistory = new ArrayList<>();
    private Set<String> processedReferenceIds = new HashSet<>();
    private List<String> failedTransactionLog = new ArrayList<>();

    // ---------- Managing customers and accounts ----------

    public void addCustomer(Customer customer) {
        customers.put(customer.getCustomerId(), customer);

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
        return accountsByNumber.get(accountNumber);
    }

    public List<Account> getAccountsOfCustomer(int customerId) {
        return accountsByCustomer.get(customerId);
    }

    // ---------- Processing a payment ----------

    public void processPayment(String accountNumber, Transaction transaction) {
        String ref = transaction.getReferenceId();


        Account account = findAccount(accountNumber);
        if (account == null) {
            logFailure(ref, "Account " + accountNumber + " not found");
            return;
        }


        if (processedReferenceIds.contains(ref)) {
            logFailure(ref, "DUPLICATE transaction, reference already processed");
            return;
        }


        double balanceBefore = account.getBalance();
        account.processTransaction(transaction);
        double balanceAfter = account.getBalance();

        if (balanceAfter != balanceBefore) {

            processedReferenceIds.add(ref);
            transactionHistory.add(transaction);
        } else {

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
