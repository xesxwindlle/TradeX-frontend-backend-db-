package com.tradex.dto.response;

public class HoldingResponse {

    private String symbol;
    private double unitPrice;
    private double quantity;
    private double currentPrice;
    private double totalValue;
    private double unrealizedPnl;

    public HoldingResponse(String symbol, double unitPrice, double quantity, double currentPrice) {
        this.symbol = symbol;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.currentPrice = currentPrice;
        this.totalValue = currentPrice * quantity;
        this.unrealizedPnl = (currentPrice - unitPrice) * quantity;
    }

    public String getSymbol() { return symbol; }
    public double getUnitPrice() { return unitPrice; }
    public double getQuantity() { return quantity; }
    public double getCurrentPrice() { return currentPrice; }
    public double getTotalValue() { return totalValue; }
    public double getUnrealizedPnl() { return unrealizedPnl; }
}
