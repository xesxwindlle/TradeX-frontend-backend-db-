package com.tradex.dto.request;

import jakarta.validation.constraints.Positive;

public class AddFundRequest {

    @Positive
    private double amount;

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
