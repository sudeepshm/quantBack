package com.quantback.execution;

import com.quantback.data.Candle;
import com.quantback.order.Order;
import com.quantback.order.OrderSide;
import com.quantback.trade.Trade;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Simulates historical order execution including configurable slippage and transaction fees.
 */
public class SimulatedExecutionEngine implements ExecutionEngine {

    private final BigDecimal slippagePerUnit;
    private final BigDecimal feePerOrder;

    public SimulatedExecutionEngine() {
        this(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public SimulatedExecutionEngine(BigDecimal slippagePerUnit, BigDecimal feePerOrder) {
        this.slippagePerUnit = Objects.requireNonNull(slippagePerUnit, "Slippage cannot be null");
        this.feePerOrder = Objects.requireNonNull(feePerOrder, "Fee cannot be null");

        if (slippagePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Slippage cannot be negative: " + slippagePerUnit);
        }
        if (feePerOrder.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fee cannot be negative: " + feePerOrder);
        }
    }

    @Override
    public Trade execute(Order order, Candle marketCandle) {
        Objects.requireNonNull(order, "Order cannot be null");
        Objects.requireNonNull(marketCandle, "Market candle cannot be null");

        BigDecimal basePrice = marketCandle.getClose();
        BigDecimal executedPrice;

        if (order.getSide() == OrderSide.BUY) {
            executedPrice = basePrice.add(slippagePerUnit);
        } else {
            executedPrice = basePrice.subtract(slippagePerUnit);
            if (executedPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("Execution price after slippage cannot be non-positive: " + executedPrice);
            }
        }

        return new Trade(
                order.getId(),
                order.getSymbol(),
                order.getSide(),
                order.getQuantity(),
                executedPrice,
                feePerOrder,
                marketCandle.getTimestamp()
        );
    }

    public BigDecimal getSlippagePerUnit() {
        return slippagePerUnit;
    }

    public BigDecimal getFeePerOrder() {
        return feePerOrder;
    }
}
