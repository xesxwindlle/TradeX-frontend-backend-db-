package com.tradex.model;

import com.tradex.enums.AccountStatus;
import com.tradex.enums.Role;

import java.time.LocalDate;

public class Account {

    private int accountNumber;
    private String email;
    private String loginPassword;
    private LocalDate createdDate;
    private double cashBalance;
    private double marketValue;
    private String linkedAccountInstitutionName;
    private Integer linkedAccountNumber;
    private Role role;
    private AccountStatus status;
    private String verificationCode;

    public Account() {}

    public int getAccountNumber() { return accountNumber; }
    public void setAccountNumber(int accountNumber) { this.accountNumber = accountNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLoginPassword() { return loginPassword; }
    public void setLoginPassword(String loginPassword) { this.loginPassword = loginPassword; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public double getCashBalance() { return cashBalance; }
    public void setCashBalance(double cashBalance) { this.cashBalance = cashBalance; }

    public double getMarketValue() { return marketValue; }
    public void setMarketValue(double marketValue) { this.marketValue = marketValue; }

    public String getLinkedAccountInstitutionName() { return linkedAccountInstitutionName; }
    public void setLinkedAccountInstitutionName(String linkedAccountInstitutionName) {
        this.linkedAccountInstitutionName = linkedAccountInstitutionName;
    }

    public Integer getLinkedAccountNumber() { return linkedAccountNumber; }
    public void setLinkedAccountNumber(Integer linkedAccountNumber) { this.linkedAccountNumber = linkedAccountNumber; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
}
