package org.igirepay.model;

import java.time.LocalDateTime;

// Transaction records one money movement.
// The referenceId is UNIQUE for every payment request. Later, we will check
// it before saving, so a retried request with the same referenceId is
// rejected instead of charging the customer twice.
public class Transaction {

    private int transactionId;
    private String referenceId;
    private double amount;
    private String transactionType;   // e.g. "SEND", "DEPOSIT", "WITHDRAW"
    private LocalDateTime timestamp;

    // Constructor: the timestamp is set automatically to "now"
    public Transaction(int transactionId, String referenceId, double amount, String transactionType) {
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now();
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Transaction{id=" + transactionId
                + ", reference=" + referenceId
                + ", amount=" + amount
                + ", type=" + transactionType
                + ", time=" + timestamp + "}";
    }
}
