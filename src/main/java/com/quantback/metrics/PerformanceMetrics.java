package com.quantback.metrics;

import java.math.BigDecimal;

/**
 * Encapsulates computed performance and risk metrics of a backtest.
 */
public class PerformanceMetrics {

    private final BigDecimal initialCapital;
    private final BigDecimal finalCapital;
    private final BigDecimal totalPnl;
    private final double totalReturnPercent;
    private final int totalTrades;
    private final int winningTrades;
    private final int losingTrades;
    private final double winRatePercent;
    private final double profitFactor;
    private final double maxDrawdownPercent;
    private final double sharpeRatio;

    public PerformanceMetrics(
            BigDecimal initialCapital,
            BigDecimal finalCapital,
            BigDecimal totalPnl,
            double totalReturnPercent,
            int totalTrades,
            int winningTrades,
            int losingTrades,
            double winRatePercent,
            double profitFactor,
            double maxDrawdownPercent,
            double sharpeRatio) {

        this.initialCapital = initialCapital;
        this.finalCapital = finalCapital;
        this.totalPnl = totalPnl;
        this.totalReturnPercent = totalReturnPercent;
        this.totalTrades = totalTrades;
        this.winningTrades = winningTrades;
        this.losingTrades = losingTrades;
        this.winRatePercent = winRatePercent;
        this.profitFactor = profitFactor;
        this.maxDrawdownPercent = maxDrawdownPercent;
        this.sharpeRatio = sharpeRatio;
    }

    public BigDecimal getInitialCapital() {
        return initialCapital;
    }

    public BigDecimal getFinalCapital() {
        return finalCapital;
    }

    public BigDecimal getTotalPnl() {
        return totalPnl;
    }

    public double getTotalReturnPercent() {
        return totalReturnPercent;
    }

    public int getTotalTrades() {
        return totalTrades;
    }

    public int getWinningTrades() {
        return winningTrades;
    }

    public int getLosingTrades() {
        return losingTrades;
    }

    public double getWinRatePercent() {
        return winRatePercent;
    }

    public double getProfitFactor() {
        return profitFactor;
    }

    public double getMaxDrawdownPercent() {
        return maxDrawdownPercent;
    }

    public double getSharpeRatio() {
        return sharpeRatio;
    }

    @Override
    public String toString() {
        return "PerformanceMetrics{" +
                "initialCapital=" + initialCapital +
                ", finalCapital=" + finalCapital +
                ", totalPnl=" + totalPnl +
                ", totalReturn=" + String.format("%.2f%%", totalReturnPercent) +
                ", totalTrades=" + totalTrades +
                ", winningTrades=" + winningTrades +
                ", losingTrades=" + losingTrades +
                ", winRate=" + String.format("%.2f%%", winRatePercent) +
                ", profitFactor=" + String.format("%.2f", profitFactor) +
                ", maxDrawdown=" + String.format("%.2f%%", maxDrawdownPercent) +
                ", sharpeRatio=" + String.format("%.2f", sharpeRatio) +
                '}';
    }
}
