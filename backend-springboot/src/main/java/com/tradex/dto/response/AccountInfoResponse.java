package com.tradex.dto.response;

import java.time.LocalDate;

public class AccountInfoResponse {

    private int accountNumber;
    private String email;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthDate;
    private String reasonForSignup;
    private LocalDate createdDate;
    private double cashBalance;
    private double marketValue;
    private String role;
    private String status;

    public int getAccountNumber() { return accountNumber; }
    public void setAccountNumber(int accountNumber) { this.accountNumber = accountNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getReasonForSignup() { return reasonForSignup; }
    public void setReasonForSignup(String reasonForSignup) { this.reasonForSignup = reasonForSignup; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public double getCashBalance() { return cashBalance; }
    public void setCashBalance(double cashBalance) { this.cashBalance = cashBalance; }

    public double getMarketValue() { return marketValue; }
    public void setMarketValue(double marketValue) { this.marketValue = marketValue; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
