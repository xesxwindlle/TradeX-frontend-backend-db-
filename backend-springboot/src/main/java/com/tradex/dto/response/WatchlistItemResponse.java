package com.tradex.dto.response;

public class WatchlistItemResponse {

    private String symbol;
    private String name;
    private double currentPrice;
    private double changePercent;

    public WatchlistItemResponse(String symbol, String name, double currentPrice, double changePercent) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
        this.changePercent = changePercent;
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getCurrentPrice() { return currentPrice; }
    public double getChangePercent() { return changePercent; }
}
