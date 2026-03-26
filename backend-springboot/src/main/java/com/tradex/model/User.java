package com.tradex.model;

import java.time.LocalDate;

public class User {

    private int id;
    private String email;
    private String phoneNumber;
    private int accountNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate birthDate;
    private String reasonForSignup;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getAccountNumber() { return accountNumber; }
    public void setAccountNumber(int accountNumber) { this.accountNumber = accountNumber; }

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
}
