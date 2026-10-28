import org.igirepay.dao.*;
import org.igirepay.model.*;

// Lab2Main tests all the database operations.
//
// BEFORE RUNNING: run schema.sql so the tables are empty.
// (If you run this file twice without resetting, the emails and reference IDs
//  already exist, so the database will reject them. That is the rules working!)
public class Lab2Main {
    public static void main(String[] args) {

        CustomerDAO customerDAO = new CustomerDAO();
        AccountDAO accountDAO = new AccountDAO();
        TransactionDAO transactionDAO = new TransactionDAO();

        // 1. Add two customers
        Customer alice = new Customer(0, "Alice Uwase", "0788000001", "alice@example.com");
        Customer bob = new Customer(0, "Bob Mugisha", "0788000002", "bob@example.com");
        customerDAO.addCustomer(alice);   // the database gives Alice her id
        customerDAO.addCustomer(bob);
        System.out.println();

        // 2. Create accounts (the method returns the new account id)
        int aliceWallet = accountDAO.createAccount(alice.getCustomerId(), "WALLET", 0);
        int bobWallet = accountDAO.createAccount(bob.getCustomerId(), "WALLET", 0);
        int unusedSavings = accountDAO.createAccount(bob.getCustomerId(), "SAVINGS", 0);
        System.out.println();

        // 3. Deposit
        accountDAO.deposit(aliceWallet, 100000, "REF-1001");

        // 4. Same reference again (like a network retry): rejected as a duplicate
        accountDAO.deposit(aliceWallet, 100000, "REF-1001");

        // 5. Withdraw
        accountDAO.withdraw(aliceWallet, 20000, "REF-1002");

        // 6. Withdraw too much: fails
        accountDAO.withdraw(aliceWallet, 999999, "REF-1003");

        // 7. Send money from Alice to Bob
        accountDAO.sendMoney(aliceWallet, bobWallet, 30000, "REF-1004");
        System.out.println();

        System.out.println("Alice balance: " + accountDAO.getBalance(aliceWallet));  // 100000 - 20000 - 30000 = 50000
        System.out.println("Bob balance:   " + accountDAO.getBalance(bobWallet));    // 30000
        System.out.println();

        // 8. View transaction history
        transactionDAO.viewTransactionHistory(aliceWallet);
        transactionDAO.viewTransactionHistory(bobWallet);
        System.out.println();

        // 8b. Show the processed reference IDs
        ProcessedRequestDAO processedRequestDAO = new ProcessedRequestDAO();
        processedRequestDAO.viewAll();
        System.out.println("Is REF-1001 processed? " + processedRequestDAO.isProcessed("REF-1001"));
        System.out.println("Is REF-9999 processed? " + processedRequestDAO.isProcessed("REF-9999"));
        System.out.println();

        // 9. Update customer details
        alice.setEmail("alice.uwase@example.com");
        customerDAO.updateCustomer(alice);
        System.out.println();

        // 10. Delete inactive accounts (Bob's empty savings account is removed)
        accountDAO.deleteInactiveAccounts();
        System.out.println("Deleted savings account still has a balance? " + accountDAO.getBalance(unusedSavings)
                + "  (-1 means it no longer exists)");
    }
}