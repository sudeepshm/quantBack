package com.quantback.backtest;

import com.quantback.strategy.multi.MultiAssetStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable configuration parameters for a multi-asset backtest simulation.
 */
public class MultiAssetBacktestRequest {

    private final List<String> symbols;
    private final MultiAssetStrategy strategy;
    private final BigDecimal initialCapital;
    private final BigDecimal slippage;
    private final BigDecimal transactionFee;
    private final int orderQuantity;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public MultiAssetBacktestRequest(
            List<String> symbols,
            MultiAssetStrategy strategy,
            BigDecimal initialCapital,
            BigDecimal slippage,
            BigDecimal transactionFee,
            int orderQuantity,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        Objects.requireNonNull(symbols, "Symbols cannot be null");
        if (symbols.isEmpty()) {
            throw new IllegalArgumentException("Multi-asset backtest requires at least one symbol");
        }
        Objects.requireNonNull(strategy, "Strategy cannot be null");
        Objects.requireNonNull(initialCapital, "Initial capital cannot be null");
        if (initialCapital.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Initial capital must be positive: " + initialCapital);
        }
        Objects.requireNonNull(slippage, "Slippage cannot be null");
        if (slippage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Slippage cannot be negative: " + slippage);
        }
        Objects.requireNonNull(transactionFee, "Transaction fee cannot be null");
        if (transactionFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Transaction fee cannot be negative: " + transactionFee);
        }
        if (orderQuantity <= 0) {
            throw new IllegalArgumentException("Order quantity must be positive: " + orderQuantity);
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        this.symbols = List.copyOf(symbols);
        this.strategy = strategy;
        this.initialCapital = initialCapital;
        this.slippage = slippage;
        this.transactionFee = transactionFee;
        this.orderQuantity = orderQuantity;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public List<String> getSymbols() { return symbols; }
    public MultiAssetStrategy getStrategy() { return strategy; }
    public BigDecimal getInitialCapital() { return initialCapital; }
    public BigDecimal getSlippage() { return slippage; }
    public BigDecimal getTransactionFee() { return transactionFee; }
    public int getOrderQuantity() { return orderQuantity; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
}
