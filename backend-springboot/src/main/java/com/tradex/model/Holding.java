package com.tradex.model;

public class Holding {

    private int accountNumber;
    private String symbol;
    private double unitPrice;
    private double quantity;

    public Holding() {}

    public Holding(int accountNumber, String symbol, double unitPrice, double quantity) {
        this.accountNumber = accountNumber;
        this.symbol = symbol;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public int getAccountNumber() { return accountNumber; }
    public void setAccountNumber(int accountNumber) { this.accountNumber = accountNumber; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
}
