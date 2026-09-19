package com.quantback.metrics;

import com.quantback.order.OrderSide;
import com.quantback.portfolio.Portfolio;
import com.quantback.portfolio.PortfolioSnapshot;
import com.quantback.trade.Trade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

/**
 * Calculates comprehensive trading and risk metrics from backtest results.
 */
public class PerformanceMetricsCalculator {

    private static final double TRADING_DAYS_PER_YEAR = 252.0;

    public PerformanceMetrics calculate(Portfolio portfolio) {
        Objects.requireNonNull(portfolio, "Portfolio cannot be null");

        BigDecimal initialCapital = portfolio.getInitialCapital();
        List<PortfolioSnapshot> snapshots = portfolio.getSnapshots();

        BigDecimal finalCapital = snapshots.isEmpty()
                ? portfolio.getCash()
                : snapshots.get(snapshots.size() - 1).getTotalValue();

        BigDecimal totalPnl = finalCapital.subtract(initialCapital);
        double totalReturnPercent = calculateTotalReturn(initialCapital, finalCapital);

        // Trade statistics (round-trip analysis)
        List<BigDecimal> tradePnls = calculateClosedTradePnls(portfolio.getTrades());
        int totalTrades = tradePnls.size();
        int winningTrades = 0;
        int losingTrades = 0;
        BigDecimal grossProfit = BigDecimal.ZERO;
        BigDecimal grossLoss = BigDecimal.ZERO;

        for (BigDecimal pnl : tradePnls) {
            if (pnl.compareTo(BigDecimal.ZERO) > 0) {
                winningTrades++;
                grossProfit = grossProfit.add(pnl);
            } else if (pnl.compareTo(BigDecimal.ZERO) < 0) {
                losingTrades++;
                grossLoss = grossLoss.add(pnl.abs());
            }
        }

        double winRate = totalTrades > 0 ? ((double) winningTrades / totalTrades) * 100.0 : 0.0;
        double profitFactor = calculateProfitFactor(grossProfit, grossLoss);
        double maxDrawdown = calculateMaxDrawdown(snapshots, initialCapital);
        double sharpeRatio = calculateSharpeRatio(snapshots);

        return new PerformanceMetrics(
                initialCapital,
                finalCapital,
                totalPnl,
                totalReturnPercent,
                totalTrades,
                winningTrades,
                losingTrades,
                winRate,
                profitFactor,
                maxDrawdown,
                sharpeRatio
        );
    }

    private double calculateTotalReturn(BigDecimal initial, BigDecimal current) {
        if (initial.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        return current.subtract(initial)
                .divide(initial, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private double calculateProfitFactor(BigDecimal grossProfit, BigDecimal grossLoss) {
        if (grossLoss.compareTo(BigDecimal.ZERO) == 0) {
            return grossProfit.compareTo(BigDecimal.ZERO) > 0 ? 999.0 : 0.0;
        }
        return grossProfit.divide(grossLoss, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private double calculateMaxDrawdown(List<PortfolioSnapshot> snapshots, BigDecimal initialCapital) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        BigDecimal peak = initialCapital;
        BigDecimal maxDrawdownPercent = BigDecimal.ZERO;

        for (PortfolioSnapshot snapshot : snapshots) {
            BigDecimal value = snapshot.getTotalValue();
            if (value.compareTo(peak) > 0) {
                peak = value;
            } else if (peak.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal drawdown = peak.subtract(value)
                        .divide(peak, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                if (drawdown.compareTo(maxDrawdownPercent) > 0) {
                    maxDrawdownPercent = drawdown;
                }
            }
        }

        return maxDrawdownPercent.doubleValue();
    }

    private double calculateSharpeRatio(List<PortfolioSnapshot> snapshots) {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < snapshots.size(); i++) {
            BigDecimal prev = snapshots.get(i - 1).getTotalValue();
            BigDecimal curr = snapshots.get(i).getTotalValue();
            if (prev.compareTo(BigDecimal.ZERO) > 0) {
                double r = curr.subtract(prev).divide(prev, 6, RoundingMode.HALF_UP).doubleValue();
                returns.add(r);
            }
        }

        if (returns.size() < 2) {
            return 0.0;
        }

        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream()
                .mapToDouble(r -> Math.pow(r - mean, 2))
                .sum() / (returns.size() - 1);

        double stdDev = Math.sqrt(variance);
        if (stdDev == 0.0) {
            return 0.0;
        }

        return (mean / stdDev) * Math.sqrt(TRADING_DAYS_PER_YEAR);
    }

    private List<BigDecimal> calculateClosedTradePnls(List<Trade> trades) {
        List<BigDecimal> pnls = new ArrayList<>();
        Queue<Trade> buyQueue = new LinkedList<>();

        for (Trade trade : trades) {
            if (trade.getSide() == OrderSide.BUY) {
                buyQueue.offer(trade);
            } else if (trade.getSide() == OrderSide.SELL && !buyQueue.isEmpty()) {
                Trade buyTrade = buyQueue.poll();
                // PnL = (sellPrice - buyPrice) * quantity - buyFee - sellFee
                BigDecimal grossPnl = trade.getPrice().subtract(buyTrade.getPrice())
                        .multiply(BigDecimal.valueOf(trade.getQuantity()));
                BigDecimal netPnl = grossPnl.subtract(buyTrade.getFee()).subtract(trade.getFee());
                pnls.add(netPnl);
            }
        }
        return pnls;
    }
}
