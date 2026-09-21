package com.quantback.api.dto;

/**
 * JSON request body for POST /api/backtests
 */
public class BacktestRequestDto {

    private String symbol;
    private String strategy;
    private double initialCapital;
    private int fastPeriod;
    private int slowPeriod;
    private double slippagePercent;
    private double transactionFeePercent;
    private int orderQuantity;
    private String startDate;
    private String endDate;

    public BacktestRequestDto() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public double getInitialCapital() { return initialCapital; }
    public void setInitialCapital(double initialCapital) { this.initialCapital = initialCapital; }

    public int getFastPeriod() { return fastPeriod; }
    public void setFastPeriod(int fastPeriod) { this.fastPeriod = fastPeriod; }

    public int getSlowPeriod() { return slowPeriod; }
    public void setSlowPeriod(int slowPeriod) { this.slowPeriod = slowPeriod; }

    public double getSlippagePercent() { return slippagePercent; }
    public void setSlippagePercent(double slippagePercent) { this.slippagePercent = slippagePercent; }

    public double getTransactionFeePercent() { return transactionFeePercent; }
    public void setTransactionFeePercent(double transactionFeePercent) { this.transactionFeePercent = transactionFeePercent; }

    public int getOrderQuantity() { return orderQuantity; }
    public void setOrderQuantity(int orderQuantity) { this.orderQuantity = orderQuantity; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
