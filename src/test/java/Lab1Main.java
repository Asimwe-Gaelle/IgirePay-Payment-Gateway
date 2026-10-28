import org.igirepay.model.Customer;
import org.igirepay.model.*;
import org.igirepay.model.manager.PaymentManager;

// Main tests the PaymentManager, including duplicate detection.
public class Lab1Main {
    public static void main(String[] args) {

        PaymentManager manager = new PaymentManager();

        // 1. Create a customer with two accounts
        Customer alice = new Customer(1, "Alice Uwase", "0788000000", "alice@example.com");
        manager.addCustomer(alice);
        manager.addAccount(1, new WalletAccount(101, "W-1001", 50000, "1234"));
        manager.addAccount(1, new SavingsAccount(102, "S-1001", 200000, 5.0));

        manager.printCustomerAccounts(1);
        System.out.println();

        // 2. A normal payment: works
        manager.processPayment("W-1001", new Transaction(1, "REF-0001", 20000, "DEPOSIT"));

        // 3. The SAME reference ID again (like a network retry): rejected as duplicate
        manager.processPayment("W-1001", new Transaction(2, "REF-0001", 20000, "DEPOSIT"));

        // 4. A new payment with a new reference ID: works
        manager.processPayment("W-1001", new Transaction(3, "REF-0002", 10000, "SEND"));

        // 5. Not enough money: fails
        manager.processPayment("W-1001", new Transaction(4, "REF-0003", 999999, "WITHDRAW"));

        // 6. Savings does not allow SEND: fails
        manager.processPayment("S-1001", new Transaction(5, "REF-0004", 5000, "SEND"));

        // 7. Account that does not exist: fails
        manager.processPayment("X-9999", new Transaction(6, "REF-0005", 1000, "DEPOSIT"));

        // 8. Show the results
        System.out.println();
        manager.printTransactionHistory();
        System.out.println();
        manager.printProcessedReferenceIds();
        System.out.println();
        manager.printFailedLog();
        System.out.println();
        manager.printCustomerAccounts(1);
    }
}