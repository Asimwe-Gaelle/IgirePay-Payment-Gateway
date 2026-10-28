package org.igirepay.model;

public class Account {

    private int accountId;
    private String accountNumber;
    private double balance;

    public Account(int accountId, String accountNumber, double balance) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Deposit failed: amount must be more than 0");
            return false;
        }
            balance += amount;
            System.out.println("Deposited " + amount + " into Account.");
            return true;
    }


    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdraw failed: amount must be more than 0");
            return false;
        }
        if (amount > balance) {
            System.out.println("Withdraw failed: not enough money");
            return false;
        }
        balance = balance - amount;
            return true;
    }


    public boolean processTransaction(Transaction transaction) {
        String type = transaction.getTransactionType();
        double amount = transaction.getAmount();

        if (type.equals("DEPOSIT")) {
            return deposit(amount);
        } else if (type.equals("WITHDRAW") || type.equals("SEND")) {
            return withdraw(amount);
        } else {
            System.out.println("Unknown transaction type: " + type);
            return false;
        }
    }

    @Override
    public String toString() {
        return "Account{id=" + accountId
                + ", number=" + accountNumber
                + ", balance=" + balance + "}";
    }
}