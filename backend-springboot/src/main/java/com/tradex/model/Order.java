package com.tradex.model;

import com.tradex.enums.OrderAction;
import com.tradex.enums.OrderStatus;

import java.time.LocalDateTime;

public class Order {

    private String id;
    private int accountNumber;
    private String symbol;
    private double unitPrice;
    private double quantity;
    private OrderAction action;
    private LocalDateTime createdTime;
    private OrderStatus status;

    public Order() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getAccountNumber() { return accountNumber; }
    public void setAccountNumber(int accountNumber) { this.accountNumber = accountNumber; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public OrderAction getAction() { return action; }
    public void setAction(OrderAction action) { this.action = action; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
