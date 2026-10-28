package org.igirepay.model;

// SavingsAccount: has withdrawal limits and a fee, and cannot do instant transfers.
public class SavingsAccount extends Account {

    private double interestRate;

    // Simple rules (constants never change)
    private static final double MAX_WITHDRAWAL = 1000000;  // limit per withdrawal
    private static final double WITHDRAWAL_FEE = 5000.0;     // fee for each withdrawal

    public SavingsAccount(int accountId, String accountNumber, double balance, double interestRate) {
        super(accountId, accountNumber, balance);
        this.interestRate = interestRate;
    }

    // OVERRIDE deposit: same as parent, with a message
    @Override
    public boolean deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Savings: deposit failed, amount must be more than 0");
            return false;
        }
        super.deposit(amount);
        System.out.println("Savings: deposit of " + amount + " saved");
        return  true;
    }

    // OVERRIDE withdraw: check the limit, then charge a fee
    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Savings: withdrawal failed, amount must be more than 0");
            return false;
        }

        if (amount > MAX_WITHDRAWAL) {
            System.out.println("Savings: withdrawal failed, limit is " + MAX_WITHDRAWAL);
            return false;
        }

        double totalAmount = amount + WITHDRAWAL_FEE;   // amount + fee
        if (totalAmount > getBalance()) {
            System.out.println("Savings: withdrawal failed, not enough money (fee is " + WITHDRAWAL_FEE + ")");
            return false;
        }

        // Use super.withdraw to take out the total (amount + fee)
        super.withdraw(totalAmount);
        System.out.println("Savings: withdrew " + amount + " + fee " + WITHDRAWAL_FEE);
        return true;
    }

    public void applyAnnualInterest() {
        double interestEarned = getBalance() * interestRate;
        setBalance(getBalance() + interestEarned);

        System.out.println("[Savings] Annual interest applied (" + (interestRate * 100) + "%): Earned $" +
                interestEarned + ". New Balance: " + getBalance());

    }

    // OVERRIDE processTransaction: savings does NOT allow instant transfers (SEND)
    @Override
    public boolean processTransaction(Transaction transaction) {
        if (transaction.getTransactionType().equals("SEND")) {
            System.out.println("Savings: SEND is not allowed. Move money to a wallet first.");
            return false;
        }
        super.processTransaction(transaction);
        return true;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
          this.interestRate = interestRate;
    }

    @Override
    public String toString() {
        return "SavingsAccount{id=" + getAccountId()
                + ", number=" + getAccountNumber()
                + ", balance=" + getBalance()
                + ", interestRate=" + interestRate + "%}";
    }
}

