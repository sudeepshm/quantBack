package com.quantback.backtest;

import com.quantback.strategy.Strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents the configuration parameters for running a backtest.
 */
public class BacktestRequest {

    private final String symbol;
    private final Strategy strategy;
    private final BigDecimal initialCapital;
    private final BigDecimal slippage;
    private final BigDecimal transactionFee;
    private final int orderQuantity;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    // Risk management parameters
    private final double stopLossPercent;
    private final double takeProfitPercent;
    private final double trailingStopPercent;

    public BacktestRequest(
            String symbol,
            Strategy strategy,
            BigDecimal initialCapital) {
        this(symbol, strategy, initialCapital, BigDecimal.ZERO, BigDecimal.ZERO, 10, null, null, 0.0, 0.0, 0.0);
    }

    public BacktestRequest(
            String symbol,
            Strategy strategy,
            BigDecimal initialCapital,
            BigDecimal slippage,
            BigDecimal transactionFee,
            int orderQuantity,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        this(symbol, strategy, initialCapital, slippage, transactionFee, orderQuantity, startDate, endDate, 0.0, 0.0, 0.0);
    }

    public BacktestRequest(
            String symbol,
            Strategy strategy,
            BigDecimal initialCapital,
            BigDecimal slippage,
            BigDecimal transactionFee,
            int orderQuantity,
            LocalDateTime startDate,
            LocalDateTime endDate,
            double stopLossPercent,
            double takeProfitPercent,
            double trailingStopPercent) {

        this.symbol = Objects.requireNonNull(symbol, "Symbol cannot be null");
        this.strategy = Objects.requireNonNull(strategy, "Strategy cannot be null");
        this.initialCapital = Objects.requireNonNull(initialCapital, "Initial capital cannot be null");
        this.slippage = Objects.requireNonNull(slippage, "Slippage cannot be null");
        this.transactionFee = Objects.requireNonNull(transactionFee, "Transaction fee cannot be null");

        if (initialCapital.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Initial capital must be positive: " + initialCapital);
        }
        if (orderQuantity <= 0) {
            throw new IllegalArgumentException("Order quantity must be positive: " + orderQuantity);
        }

        this.orderQuantity = orderQuantity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.stopLossPercent = Math.max(0.0, stopLossPercent);
        this.takeProfitPercent = Math.max(0.0, takeProfitPercent);
        this.trailingStopPercent = Math.max(0.0, trailingStopPercent);
    }

    public String getSymbol() {
        return symbol;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public BigDecimal getInitialCapital() {
        return initialCapital;
    }

    public BigDecimal getSlippage() {
        return slippage;
    }

    public BigDecimal getTransactionFee() {
        return transactionFee;
    }

    public int getOrderQuantity() {
        return orderQuantity;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public double getStopLossPercent() {
        return stopLossPercent;
    }

    public double getTakeProfitPercent() {
        return takeProfitPercent;
    }

    public double getTrailingStopPercent() {
        return trailingStopPercent;
    }
}
