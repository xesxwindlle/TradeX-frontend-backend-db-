package com.tradex.dto.response;

import java.time.LocalDate;

public class UserSummaryResponse {

    private int id;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate birthDate;
    private String reasonForSignup;
    private int accountNumber;
    private LocalDate createdDate;
    private double cashBalance;
    private double marketValue;
    private String role;
    private String status;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getReasonForSignup() { return reasonForSignup; }
    public void setReasonForSignup(String reasonForSignup) { this.reasonForSignup = reasonForSignup; }

    public int getAccountNumber() { return accountNumber; }
    public void setAccountNumber(int accountNumber) { this.accountNumber = accountNumber; }

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
