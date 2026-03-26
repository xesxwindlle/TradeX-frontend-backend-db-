package com.tradex.dto.response;

public class AuthResponse {

    private String token;
    private String email;
    private int accountNumber;
    private String role;
    private String status;

    public AuthResponse(String token, String email, int accountNumber, String role, String status) {
        this.token = token;
        this.email = email;
        this.accountNumber = accountNumber;
        this.role = role;
        this.status = status;
    }

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public int getAccountNumber() { return accountNumber; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
}
