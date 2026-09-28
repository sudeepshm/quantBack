package com.quantback.order;

import com.quantback.data.Candle;
import com.quantback.portfolio.Portfolio;
import com.quantback.portfolio.Position;
import com.quantback.strategy.Signal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Responsible for translating strategy signals into valid, risk-checked Order instructions.
 * Supports configurable Stop-Loss, Take-Profit, and Trailing-Stop risk controls.
 */
public class OrderManager {

    private final int defaultOrderQuantity;
    private final double stopLossPercent;
    private final double takeProfitPercent;
    private final double trailingStopPercent;

    private final Map<String, BigDecimal> peakPrices = new HashMap<>();

    public OrderManager() {
        this(10, 0.0, 0.0, 0.0);
    }

    public OrderManager(int defaultOrderQuantity) {
        this(defaultOrderQuantity, 0.0, 0.0, 0.0);
    }

    public OrderManager(int defaultOrderQuantity, double stopLossPercent, double takeProfitPercent, double trailingStopPercent) {
        if (defaultOrderQuantity <= 0) {
            throw new IllegalArgumentException("Default order quantity must be positive: " + defaultOrderQuantity);
        }
        if (stopLossPercent < 0) {
            throw new IllegalArgumentException("Stop-loss percent cannot be negative: " + stopLossPercent);
        }
        if (takeProfitPercent < 0) {
            throw new IllegalArgumentException("Take-profit percent cannot be negative: " + takeProfitPercent);
        }
        if (trailingStopPercent < 0) {
            throw new IllegalArgumentException("Trailing-stop percent cannot be negative: " + trailingStopPercent);
        }

        this.defaultOrderQuantity = defaultOrderQuantity;
        this.stopLossPercent = stopLossPercent;
        this.takeProfitPercent = takeProfitPercent;
        this.trailingStopPercent = trailingStopPercent;
    }

    /**
     * Checks if the open position violates any risk bounds (Stop-Loss, Take-Profit, Trailing-Stop).
     *
     * @param symbol financial instrument ticker
     * @param candle current candle
     * @param portfolio current portfolio state
     * @return Optional containing risk exit order if triggered, empty otherwise
     */
    public Optional<Order> checkRiskExit(String symbol, Candle candle, Portfolio portfolio) {
        Optional<Position> positionOpt = portfolio.getPosition(symbol);
        if (positionOpt.isEmpty() || !positionOpt.get().isOpen()) {
            peakPrices.remove(symbol);
            return Optional.empty();
        }

        Position position = positionOpt.get();
        BigDecimal entryPrice = position.getAverageEntryPrice();
        BigDecimal currentClose = candle.getClose();
        BigDecimal currentHigh = candle.getHigh();
        BigDecimal currentLow = candle.getLow();

        // Update peak price for trailing stop
        BigDecimal peak = peakPrices.getOrDefault(symbol, entryPrice);
        if (currentHigh.compareTo(peak) > 0) {
            peak = currentHigh;
        }
        peakPrices.put(symbol, peak);

        // 1. Stop-Loss Check
        if (stopLossPercent > 0) {
            BigDecimal stopPrice = entryPrice.multiply(
                    BigDecimal.ONE.subtract(BigDecimal.valueOf(stopLossPercent / 100.0))
            );
            if (currentClose.compareTo(stopPrice) <= 0 || currentLow.compareTo(stopPrice) <= 0) {
                peakPrices.remove(symbol);
                return Optional.of(new Order(
                        symbol,
                        OrderSide.SELL,
                        OrderType.STOP_LOSS,
                        position.getQuantity(),
                        candle.getTimestamp()
                ));
            }
        }

        // 2. Take-Profit Check
        if (takeProfitPercent > 0) {
            BigDecimal targetPrice = entryPrice.multiply(
                    BigDecimal.ONE.add(BigDecimal.valueOf(takeProfitPercent / 100.0))
            );
            if (currentClose.compareTo(targetPrice) >= 0 || currentHigh.compareTo(targetPrice) >= 0) {
                peakPrices.remove(symbol);
                return Optional.of(new Order(
                        symbol,
                        OrderSide.SELL,
                        OrderType.TAKE_PROFIT,
                        position.getQuantity(),
                        candle.getTimestamp()
                ));
            }
        }

        // 3. Trailing-Stop Check
        if (trailingStopPercent > 0) {
            BigDecimal trailPrice = peak.multiply(
                    BigDecimal.ONE.subtract(BigDecimal.valueOf(trailingStopPercent / 100.0))
            );
            if (currentClose.compareTo(trailPrice) <= 0 || currentLow.compareTo(trailPrice) <= 0) {
                peakPrices.remove(symbol);
                return Optional.of(new Order(
                        symbol,
                        OrderSide.SELL,
                        OrderType.TRAILING_STOP,
                        position.getQuantity(),
                        candle.getTimestamp()
                ));
            }
        }

        return Optional.empty();
    }

    /**
     * Evaluates risk checks and strategy signals against portfolio state.
     */
    public Optional<Order> createOrder(Signal signal, String symbol, Candle candle, Portfolio portfolio) {
        Objects.requireNonNull(signal, "Signal cannot be null");
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        Objects.requireNonNull(candle, "Candle cannot be null");
        Objects.requireNonNull(portfolio, "Portfolio cannot be null");

        // Priority 1: Check risk-based exits first
        Optional<Order> riskOrder = checkRiskExit(symbol, candle, portfolio);
        if (riskOrder.isPresent()) {
            return riskOrder;
        }

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
                quantityToBuy = portfolio.getCash().divide(price, 0, RoundingMode.DOWN).intValue();
            }

            if (quantityToBuy <= 0) {
                return Optional.empty();
            }

            peakPrices.put(symbol, candle.getClose());

            return Optional.of(new Order(
                    symbol,
                    OrderSide.BUY,
                    OrderType.MARKET,
                    quantityToBuy,
                    candle.getTimestamp()
            ));
        }

        if (signal == Signal.SELL) {
            Optional<Position> positionOpt = portfolio.getPosition(symbol);
            if (positionOpt.isEmpty() || !positionOpt.get().isOpen()) {
                return Optional.empty();
            }

            peakPrices.remove(symbol);
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
