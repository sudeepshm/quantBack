package com.quantback.backtest;

import com.quantback.metrics.PerformanceMetrics;
import com.quantback.portfolio.Portfolio;
import com.quantback.portfolio.PortfolioSnapshot;
import com.quantback.trade.Trade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates the complete result of a backtest run.
 */
public class BacktestResult {

    private final BacktestRequest request;
    private final Portfolio portfolio;
    private final PerformanceMetrics metrics;

    public BacktestResult(BacktestRequest request, Portfolio portfolio, PerformanceMetrics metrics) {
        this.request = Objects.requireNonNull(request, "Request cannot be null");
        this.portfolio = Objects.requireNonNull(portfolio, "Portfolio cannot be null");
        this.metrics = Objects.requireNonNull(metrics, "Metrics cannot be null");
    }

    public BacktestRequest getRequest() {
        return request;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public PerformanceMetrics getMetrics() {
        return metrics;
    }

    public List<Trade> getTrades() {
        return portfolio.getTrades();
    }

    public List<PortfolioSnapshot> getSnapshots() {
        return portfolio.getSnapshots();
    }

    public BigDecimal getInitialCapital() {
        return metrics.getInitialCapital();
    }

    public BigDecimal getFinalCapital() {
        return metrics.getFinalCapital();
    }

    public double getTotalReturnPercent() {
        return metrics.getTotalReturnPercent();
    }

    public void printSummary() {
        System.out.println("==========================================================");
        System.out.println("               QUANTBACK - BACKTEST REPORT                ");
        System.out.println("==========================================================");
        System.out.printf("Symbol          : %s%n", request.getSymbol());
        System.out.printf("Strategy        : %s%n", request.getStrategy().getName());
        System.out.printf("Initial Capital : ₹%,.2f%n", metrics.getInitialCapital());
        System.out.printf("Final Capital   : ₹%,.2f%n", metrics.getFinalCapital());
        System.out.printf("Total Net P&L   : ₹%,.2f%n", metrics.getTotalPnl());
        System.out.printf("Total Return    : %.2f%%%n", metrics.getTotalReturnPercent());
        System.out.printf("Total Trades    : %d (Win: %d, Loss: %d)%n",
                metrics.getTotalTrades(), metrics.getWinningTrades(), metrics.getLosingTrades());
        System.out.printf("Win Rate        : %.2f%%%n", metrics.getWinRatePercent());
        System.out.printf("Profit Factor   : %.2f%n", metrics.getProfitFactor());
        System.out.printf("Max Drawdown    : %.2f%%%n", metrics.getMaxDrawdownPercent());
        System.out.printf("Sharpe Ratio    : %.2f%n", metrics.getSharpeRatio());
        System.out.println("----------------------------------------------------------");
        System.out.printf("Executed Trades: %d%n", getTrades().size());
        for (Trade trade : getTrades()) {
            System.out.printf("  %s | %4s %3d @ ₹%,.2f | Fee: ₹%.2f%n",
                    trade.getTimestamp(), trade.getSide(), trade.getQuantity(), trade.getPrice(), trade.getFee());
        }
        System.out.println("==========================================================");
    }
}
