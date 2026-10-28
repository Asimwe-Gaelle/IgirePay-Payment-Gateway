package org.igirepay.model;

// WalletAccount: fast and simple, with INSTANT transfers and no fees.
public class WalletAccount extends Account {

    private String pin;

    public WalletAccount(int accountId, String accountNumber, double balance, String pin) {
        super(accountId, accountNumber, balance);
        this.pin = pin;
    }

    // OVERRIDE deposit: wallet deposits are instant
    @Override
    public boolean deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Wallet: deposit failed, amount must be more than 0");
            return false;
        }
        super.deposit(amount);   // reuse the parent's logic
        System.out.println("Wallet: instant deposit of " + amount);
        return true;
    }

    // OVERRIDE withdraw: wallet withdrawals are instant, no fee
    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > getBalance()) {
            System.out.println("Wallet: withdrawal failed (check the amount and your balance)");
            return false;
        }
        super.withdraw(amount);
        System.out.println("Wallet: instant withdrawal of " + amount);
        return true;
    }

    // OVERRIDE processTransaction: wallet supports instant transfers
    @Override
    public boolean processTransaction(Transaction transaction) {
        System.out.println("Wallet: processing " + transaction.getTransactionType()
                + " instantly (ref " + transaction.getReferenceId() + ")");
        super.processTransaction(transaction);
        return true;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    @Override
    public String toString() {
        return "WalletAccount{id=" + getAccountId()
                + ", number=" + getAccountNumber()
                + ", balance=" + getBalance() + "}";
    }

}

