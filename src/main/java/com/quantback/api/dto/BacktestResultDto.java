package com.quantback.api.dto;

import java.util.List;

/**
 * JSON response body for POST /api/backtests
 */
public class BacktestResultDto {

    private String symbol;
    private String strategy;
    private double initialCapital;
    private double finalCapital;
    private double totalPnl;
    private double totalReturnPercent;
    private int totalTrades;
    private int winningTrades;
    private int losingTrades;
    private double winRatePercent;
    private double profitFactor;
    private double maxDrawdownPercent;
    private double sharpeRatio;
    private List<TradeDto> trades;
    private List<SnapshotDto> equityCurve;

    public BacktestResultDto() {}

    // ---- Nested DTOs ----

    public static class TradeDto {
        private String timestamp;
        private String side;
        private int quantity;
        private double price;
        private double fee;
        private double grossValue;

        public TradeDto() {}

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public String getSide() { return side; }
        public void setSide(String side) { this.side = side; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public double getFee() { return fee; }
        public void setFee(double fee) { this.fee = fee; }

        public double getGrossValue() { return grossValue; }
        public void setGrossValue(double grossValue) { this.grossValue = grossValue; }
    }

    public static class SnapshotDto {
        private String timestamp;
        private double totalValue;
        private double cash;
        private double positionsValue;
        private double realizedPnl;
        private double unrealizedPnl;

        public SnapshotDto() {}

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public double getTotalValue() { return totalValue; }
        public void setTotalValue(double totalValue) { this.totalValue = totalValue; }

        public double getCash() { return cash; }
        public void setCash(double cash) { this.cash = cash; }

        public double getPositionsValue() { return positionsValue; }
        public void setPositionsValue(double positionsValue) { this.positionsValue = positionsValue; }

        public double getRealizedPnl() { return realizedPnl; }
        public void setRealizedPnl(double realizedPnl) { this.realizedPnl = realizedPnl; }

        public double getUnrealizedPnl() { return unrealizedPnl; }
        public void setUnrealizedPnl(double unrealizedPnl) { this.unrealizedPnl = unrealizedPnl; }
    }

    // ---- Getters / Setters ----

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public double getInitialCapital() { return initialCapital; }
    public void setInitialCapital(double initialCapital) { this.initialCapital = initialCapital; }

    public double getFinalCapital() { return finalCapital; }
    public void setFinalCapital(double finalCapital) { this.finalCapital = finalCapital; }

    public double getTotalPnl() { return totalPnl; }
    public void setTotalPnl(double totalPnl) { this.totalPnl = totalPnl; }

    public double getTotalReturnPercent() { return totalReturnPercent; }
    public void setTotalReturnPercent(double totalReturnPercent) { this.totalReturnPercent = totalReturnPercent; }

    public int getTotalTrades() { return totalTrades; }
    public void setTotalTrades(int totalTrades) { this.totalTrades = totalTrades; }

    public int getWinningTrades() { return winningTrades; }
    public void setWinningTrades(int winningTrades) { this.winningTrades = winningTrades; }

    public int getLosingTrades() { return losingTrades; }
    public void setLosingTrades(int losingTrades) { this.losingTrades = losingTrades; }

    public double getWinRatePercent() { return winRatePercent; }
    public void setWinRatePercent(double winRatePercent) { this.winRatePercent = winRatePercent; }

    public double getProfitFactor() { return profitFactor; }
    public void setProfitFactor(double profitFactor) { this.profitFactor = profitFactor; }

    public double getMaxDrawdownPercent() { return maxDrawdownPercent; }
    public void setMaxDrawdownPercent(double maxDrawdownPercent) { this.maxDrawdownPercent = maxDrawdownPercent; }

    public double getSharpeRatio() { return sharpeRatio; }
    public void setSharpeRatio(double sharpeRatio) { this.sharpeRatio = sharpeRatio; }

    public List<TradeDto> getTrades() { return trades; }
    public void setTrades(List<TradeDto> trades) { this.trades = trades; }

    public List<SnapshotDto> getEquityCurve() { return equityCurve; }
    public void setEquityCurve(List<SnapshotDto> equityCurve) { this.equityCurve = equityCurve; }
}
