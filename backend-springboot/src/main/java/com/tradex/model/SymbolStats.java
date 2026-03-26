package com.tradex.model;

public class SymbolStats {

    private String symbol;
    private int count;

    public SymbolStats() {}

    public SymbolStats(String symbol, int count) {
        this.symbol = symbol;
        this.count = count;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
