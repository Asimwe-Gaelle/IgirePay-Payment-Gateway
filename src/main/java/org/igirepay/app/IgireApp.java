package org.igirepay.app;

import org.igirepay.service.TransactionService;

import java.sql.Date;
import java.util.InputMismatchException;
import java.util.Scanner;
import org.igirepay.service.AuthenticationService;

public class  IgireApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TransactionService service = new TransactionService();
    private static final AuthenticationService authService = new AuthenticationService();

    public static void main(String[] args) throws Exception {

        int choice = 0;

        do {
            try {
                System.out.println("\n=== IgirePay Menu ===");
                System.out.println("1. Register Customer");
                System.out.println("2. Update Customer");
                System.out.println("3. Create Wallet Account");
                System.out.println("4. Create Savings Account");
                System.out.println("5. Deposit Money");
                System.out.println("6. Withdraw Money");
                System.out.println("7. Transfer Money");
                System.out.println("8. View Transactions");
                System.out.println("9. Export Transactions to CSV");
                System.out.println("10. View Daily Summary");
                System.out.println("11. Customer Transaction Statement");
                System.out.println("13. Create Account PIN");
                System.out.println("14. Validate Account PIN");
                System.out.println("15. Change Account PIN");
                System.out.println("16. Exit");
                System.out.print("Choose an option: ");

                choice = scanner.nextInt();

                switch (choice) {
                    case 1 -> registerCustomer();
                    case 2 -> updateCustomer();
                    case 3 -> createWalletAccount();
                    case 4 -> createSavingsAccount();
                    case 5 -> depositMoney();
                    case 6 -> withdrawMoney();
                    case 7 -> transferMoney();
                    case 8 -> viewTransactions();
                    case 9 -> exportTransactions();
                    case 10 -> viewDailySummary();
                    case 11 -> viewCustomerStatement();
                    case 13 -> createAccountPIN();
                    case 14 -> validateAccountPIN();
                    case 15 -> changeAccountPIN();
                    case 16 -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice!");

                }
            }catch(InputMismatchException e) {
                System.out.println("Input error: Please enter a valid number.");

            }


        } while(choice != 16);

        scanner.close();
    }

    public static void registerCustomer() throws Exception{
        System.out.print("Name: ");
        String name = scanner.next();
        System.out.print("Email: ");
        String email = scanner.next();
        System.out.print("Phone: ");
        String phone = scanner.next();
        service.registerCustomer(name, email, phone);
        System.out.println("Customer registered successfully!");
    }
    public static void updateCustomer() throws Exception {
        System.out.print("Customer ID: ");
        int id = scanner.nextInt();
        System.out.print("New Name: ");
        String name = scanner.next();
        System.out.print("New Email: ");
        String email = scanner.next();
        System.out.print("New Phone: ");
        String phone = scanner.next();
        service.updateCustomer(id, name, email, phone);
        System.out.println("Customer updated successfully!");
    }

    // --- Account Management ---
    private static void createWalletAccount() throws Exception {
        System.out.print("Customer ID: ");
        int id = scanner.nextInt();
        System.out.print("Initial Balance: ");
        double balance = scanner.nextDouble();
        service.createWalletAccount(id, balance);
        System.out.println("Wallet account created!");
    }

    private static void createSavingsAccount() throws Exception {
        System.out.print("Customer ID: ");
        int id = scanner.nextInt();
        System.out.print("Initial Balance: ");
        double balance = scanner.nextDouble();
        service.createSavingsAccount(id, balance);
        System.out.println("Savings account created!");
    }

    private static void depositMoney() throws Exception {
        System.out.print("Account ID: ");
        int id = scanner.nextInt();
        System.out.print("Reference ID: ");
        String ref = scanner.next();
        System.out.print("Amount: ");
        double amount = scanner.nextDouble();
        service.deposit(id, ref, amount);
    }

    private static void withdrawMoney() throws Exception {
        System.out.print("Account ID: ");
        int id = scanner.nextInt();
        System.out.print("Reference ID: ");
        String ref = scanner.next();
        System.out.print("Amount: ");
        double amt = scanner.nextDouble();
        service.withdraw(id, ref, amt);
    }
    private static void transferMoney() throws Exception {
        System.out.print("From Account ID: ");
        int from = scanner.nextInt();
        System.out.print("To Account ID: ");
        int to = scanner.nextInt();
        System.out.print("Reference ID: ");
        String ref = scanner.next();
        System.out.print("Amount: ");
        double amt = scanner.nextDouble();
        service.transfer(from, to, ref, amt);
    }

    private static void viewTransactions() throws Exception {
        System.out.print("Account ID: ");
        int id = scanner.nextInt();
        service.viewTransactions(id);
    }


    private static void exportTransactions() throws Exception {

        System.out.print("Enter file name (example: transactions.csv): ");
        String fileName = scanner.next();

        service.exportTransactions(fileName);
    }

    private static void viewDailySummary() throws Exception {
        System.out.print("Enter date (YYYY-MM-DD): ");
        String dateText = scanner.next();

        Date date = Date.valueOf(dateText);

        service.showDailySummary(date);
    }

    private static void viewCustomerStatement() throws Exception {
        System.out.print("Enter Customer ID: ");
        int customerId = scanner.nextInt();

        service.showCustomerStatement(customerId);
    }


    private static void createAccountPIN() throws Exception {

        System.out.print("Enter Account ID: ");
        int accountId = scanner.nextInt();

        System.out.print("Create a 4-digit PIN: ");
        String pin = scanner.next();

        authService.createPIN(accountId, pin);

        System.out.println("PIN created successfully!");
    }

    private static void validateAccountPIN() throws Exception {

        System.out.print("Enter Account ID: ");
        int accountId = scanner.nextInt();

        System.out.print("Enter your PIN: ");
        String pin = scanner.next();

        boolean correct =
                authService.validatePIN(accountId, pin);

        if (correct) {
            System.out.println("PIN is correct!");
        } else {
            System.out.println(
                    "Incorrect PIN or no PIN has been created."
            );
        }
    }

    private static void changeAccountPIN() throws Exception {
        System.out.print("Enter Account ID: ");
        int accountId = scanner.nextInt();

        System.out.print("Enter your old PIN: ");
        String oldPIN = scanner.next();

        System.out.print("Enter your new PIN: ");
        String newPIN = scanner.next();

        authService.changePIN(accountId, oldPIN, newPIN);

        System.out.println("PIN changed successfully!");
    }

}

