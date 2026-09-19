package com.quantback.order;

import com.quantback.data.Candle;
import com.quantback.portfolio.Portfolio;
import com.quantback.portfolio.Position;
import com.quantback.strategy.Signal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

/**
 * Responsible for translating strategy signals into valid, risk-checked Order instructions.
 * The strategy has no knowledge of portfolio state or order execution.
 */
public class OrderManager {

    private final int defaultOrderQuantity;

    public OrderManager() {
        this(10);
    }

    public OrderManager(int defaultOrderQuantity) {
        if (defaultOrderQuantity <= 0) {
            throw new IllegalArgumentException("Default order quantity must be positive: " + defaultOrderQuantity);
        }
        this.defaultOrderQuantity = defaultOrderQuantity;
    }

    /**
     * Evaluates a strategy signal against the current portfolio state and candle,
     * producing an executable Order if conditions are satisfied.
     *
     * @param signal strategy trading signal
     * @param symbol financial instrument ticker
     * @param candle current candle
     * @param portfolio current portfolio state
     * @return Optional containing Order if an order should be placed, empty otherwise
     */
    public Optional<Order> createOrder(Signal signal, String symbol, Candle candle, Portfolio portfolio) {
        Objects.requireNonNull(signal, "Signal cannot be null");
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        Objects.requireNonNull(candle, "Candle cannot be null");
        Objects.requireNonNull(portfolio, "Portfolio cannot be null");

        if (signal == Signal.HOLD) {
            return Optional.empty();
        }

        if (signal == Signal.BUY) {
            // Long-only V1: Do not add if already in an open position
            if (portfolio.hasOpenPosition(symbol)) {
                return Optional.empty();
            }

            BigDecimal price = candle.getClose();
            BigDecimal costForDefault = price.multiply(BigDecimal.valueOf(defaultOrderQuantity));

            int quantityToBuy = defaultOrderQuantity;
            if (portfolio.getCash().compareTo(costForDefault) < 0) {
                // If insufficient cash for default quantity, compute maximum affordable quantity
                quantityToBuy = portfolio.getCash().divide(price, 0, RoundingMode.DOWN).intValue();
            }

            if (quantityToBuy <= 0) {
                return Optional.empty(); // Not enough cash to buy even 1 unit
            }

            return Optional.of(new Order(
                    symbol,
                    OrderSide.BUY,
                    OrderType.MARKET,
                    quantityToBuy,
                    candle.getTimestamp()
            ));
        }

        if (signal == Signal.SELL) {
            // Sell to close open position
            Optional<Position> positionOpt = portfolio.getPosition(symbol);
            if (positionOpt.isEmpty() || !positionOpt.get().isOpen()) {
                return Optional.empty();
            }

            int heldQuantity = positionOpt.get().getQuantity();
            return Optional.of(new Order(
                    symbol,
                    OrderSide.SELL,
                    OrderType.MARKET,
                    heldQuantity,
                    candle.getTimestamp()
            ));
        }

        return Optional.empty();
    }

    public int getDefaultOrderQuantity() {
        return defaultOrderQuantity;
    }
}
