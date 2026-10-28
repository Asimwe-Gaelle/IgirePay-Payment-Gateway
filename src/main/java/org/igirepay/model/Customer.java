package org.igirepay.model;


// Customer represents a person who uses IgirePay.
public class Customer {

    private int customerId;
    private String fullName;
    private String phoneNumber;
    private String email;

    public Customer(int customerId, String fullName, String phoneNumber, String email) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Customer{id=" + customerId
                + ", name=" + fullName
                + ", phone=" + phoneNumber
                + ", email=" + email + "}";
    }
}

