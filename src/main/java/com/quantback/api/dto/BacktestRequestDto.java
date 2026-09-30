package com.quantback.api.dto;

import java.util.HashMap;
import java.util.Map;

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

    // Parameters for Quantpedia strategies
    private int period;
    private int lookbackPeriod;
    private double thresholdPercent;
    private double exitBufferPercent;
    private double oversoldThreshold;
    private double overboughtThreshold;
    private int daysBeforeMonthEnd;
    private int daysAfterMonthStart;
    private int numBuckets;
    private double minConsistencyRatio;
    private int entryMonth;
    private int exitMonth;
    private double stdDevMultiplier;
    private int entryDayOfWeek;
    private int exitDayOfWeek;
    private int entryPeriod;
    private int exitPeriod;
    private int signalPeriod;

    // Risk controls
    private double stopLossPercent;
    private double takeProfitPercent;
    private double trailingStopPercent;

    private Map<String, Object> extraParams = new HashMap<>();

    public BacktestRequestDto() {}

    public double getStopLossPercent() { return stopLossPercent; }
    public void setStopLossPercent(double stopLossPercent) { this.stopLossPercent = stopLossPercent; }

    public double getTakeProfitPercent() { return takeProfitPercent; }
    public void setTakeProfitPercent(double takeProfitPercent) { this.takeProfitPercent = takeProfitPercent; }

    public double getTrailingStopPercent() { return trailingStopPercent; }
    public void setTrailingStopPercent(double trailingStopPercent) { this.trailingStopPercent = trailingStopPercent; }

    public int getNumBuckets() { return numBuckets; }
    public void setNumBuckets(int numBuckets) { this.numBuckets = numBuckets; }

    public double getMinConsistencyRatio() { return minConsistencyRatio; }
    public void setMinConsistencyRatio(double minConsistencyRatio) { this.minConsistencyRatio = minConsistencyRatio; }

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

    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }

    public int getLookbackPeriod() { return lookbackPeriod; }
    public void setLookbackPeriod(int lookbackPeriod) { this.lookbackPeriod = lookbackPeriod; }

    public double getThresholdPercent() { return thresholdPercent; }
    public void setThresholdPercent(double thresholdPercent) { this.thresholdPercent = thresholdPercent; }

    public double getExitBufferPercent() { return exitBufferPercent; }
    public void setExitBufferPercent(double exitBufferPercent) { this.exitBufferPercent = exitBufferPercent; }

    public double getOversoldThreshold() { return oversoldThreshold; }
    public void setOversoldThreshold(double oversoldThreshold) { this.oversoldThreshold = oversoldThreshold; }

    public double getOverboughtThreshold() { return overboughtThreshold; }
    public void setOverboughtThreshold(double overboughtThreshold) { this.overboughtThreshold = overboughtThreshold; }

    public int getDaysBeforeMonthEnd() { return daysBeforeMonthEnd; }
    public void setDaysBeforeMonthEnd(int daysBeforeMonthEnd) { this.daysBeforeMonthEnd = daysBeforeMonthEnd; }

    public int getDaysAfterMonthStart() { return daysAfterMonthStart; }
    public void setDaysAfterMonthStart(int daysAfterMonthStart) { this.daysAfterMonthStart = daysAfterMonthStart; }

    public int getEntryMonth() { return entryMonth; }
    public void setEntryMonth(int entryMonth) { this.entryMonth = entryMonth; }

    public int getExitMonth() { return exitMonth; }
    public void setExitMonth(int exitMonth) { this.exitMonth = exitMonth; }

    public double getStdDevMultiplier() { return stdDevMultiplier; }
    public void setStdDevMultiplier(double stdDevMultiplier) { this.stdDevMultiplier = stdDevMultiplier; }

    public int getEntryDayOfWeek() { return entryDayOfWeek; }
    public void setEntryDayOfWeek(int entryDayOfWeek) { this.entryDayOfWeek = entryDayOfWeek; }

    public int getExitDayOfWeek() { return exitDayOfWeek; }
    public void setExitDayOfWeek(int exitDayOfWeek) { this.exitDayOfWeek = exitDayOfWeek; }

    public int getEntryPeriod() { return entryPeriod; }
    public void setEntryPeriod(int entryPeriod) { this.entryPeriod = entryPeriod; }

    public int getExitPeriod() { return exitPeriod; }
    public void setExitPeriod(int exitPeriod) { this.exitPeriod = exitPeriod; }

    public int getSignalPeriod() { return signalPeriod; }
    public void setSignalPeriod(int signalPeriod) { this.signalPeriod = signalPeriod; }

    public Map<String, Object> getExtraParams() { return extraParams; }
    public void setExtraParams(Map<String, Object> extraParams) { this.extraParams = extraParams; }
}
