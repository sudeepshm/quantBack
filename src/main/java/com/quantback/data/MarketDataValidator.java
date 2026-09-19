package com.quantback.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Validates market data before backtesting starts.
 * Ensures data integrity, positive prices, valid OHLC relationships,
 * and strict chronological order without duplicates.
 */
public class MarketDataValidator {

    public void validate(List<Candle> candles) {
        Objects.requireNonNull(candles, "Candle list cannot be null");
        if (candles.isEmpty()) {
            throw new IllegalArgumentException("Market data cannot be empty");
        }

        LocalDateTime lastTimestamp = null;

        for (int i = 0; i < candles.size(); i++) {
            Candle candle = candles.get(i);
            if (candle == null) {
                throw new IllegalArgumentException("Candle at index " + i + " cannot be null");
            }

            validateCandleValues(candle, i);

            // Chronological validation
            if (lastTimestamp != null) {
                if (candle.getTimestamp().isEqual(lastTimestamp)) {
                    throw new IllegalArgumentException(
                            String.format("Duplicate timestamp detected at index %d: %s", i, candle.getTimestamp())
                    );
                }
                if (candle.getTimestamp().isBefore(lastTimestamp)) {
                    throw new IllegalArgumentException(
                            String.format("Market data is not chronologically ordered at index %d: %s is before %s",
                                    i, candle.getTimestamp(), lastTimestamp)
                    );
                }
            }
            lastTimestamp = candle.getTimestamp();
        }
    }

    private void validateCandleValues(Candle candle, int index) {
        BigDecimal open = candle.getOpen();
        BigDecimal high = candle.getHigh();
        BigDecimal low = candle.getLow();
        BigDecimal close = candle.getClose();

        if (open.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(String.format("Index %d: Open price must be positive, got %s", index, open));
        }
        if (high.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(String.format("Index %d: High price must be positive, got %s", index, high));
        }
        if (low.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(String.format("Index %d: Low price must be positive, got %s", index, low));
        }
        if (close.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(String.format("Index %d: Close price must be positive, got %s", index, close));
        }

        if (high.compareTo(low) < 0) {
            throw new IllegalArgumentException(String.format("Index %d: High (%s) cannot be less than Low (%s)", index, high, low));
        }
        if (high.compareTo(open) < 0) {
            throw new IllegalArgumentException(String.format("Index %d: High (%s) cannot be less than Open (%s)", index, high, open));
        }
        if (high.compareTo(close) < 0) {
            throw new IllegalArgumentException(String.format("Index %d: High (%s) cannot be less than Close (%s)", index, high, close));
        }
        if (low.compareTo(open) > 0) {
            throw new IllegalArgumentException(String.format("Index %d: Low (%s) cannot be greater than Open (%s)", index, low, open));
        }
        if (low.compareTo(close) > 0) {
            throw new IllegalArgumentException(String.format("Index %d: Low (%s) cannot be greater than Close (%s)", index, low, close));
        }

        if (candle.getVolume() < 0) {
            throw new IllegalArgumentException(String.format("Index %d: Volume cannot be negative, got %d", index, candle.getVolume()));
        }
    }
}
