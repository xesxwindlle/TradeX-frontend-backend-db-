package com.tradex.dto.request;

import com.tradex.enums.OrderAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OrderRequest {

    @NotBlank
    private String symbol;

    @Positive
    private double unitPrice;

    @Positive
    private double quantity;

    @NotNull
    private OrderAction action;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public OrderAction getAction() { return action; }
    public void setAction(OrderAction action) { this.action = action; }
}
