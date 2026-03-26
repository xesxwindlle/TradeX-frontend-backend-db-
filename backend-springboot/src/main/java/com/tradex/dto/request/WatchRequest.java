package com.tradex.dto.request;

import jakarta.validation.constraints.NotBlank;

public class WatchRequest {

    @NotBlank
    private String symbol;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
}
