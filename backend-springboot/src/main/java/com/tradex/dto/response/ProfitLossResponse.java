package com.tradex.dto.response;

public class ProfitLossResponse {

    private double totalProfitLoss;
    private double totalCostBasis;
    private double totalMarketValue;
    private double percentage;

    public ProfitLossResponse(double totalProfitLoss, double totalCostBasis,
                               double totalMarketValue, double percentage) {
        this.totalProfitLoss = totalProfitLoss;
        this.totalCostBasis = totalCostBasis;
        this.totalMarketValue = totalMarketValue;
        this.percentage = percentage;
    }

    public double getTotalProfitLoss() { return totalProfitLoss; }
    public double getTotalCostBasis() { return totalCostBasis; }
    public double getTotalMarketValue() { return totalMarketValue; }
    public double getPercentage() { return percentage; }
}
