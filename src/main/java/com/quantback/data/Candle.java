package com.quantback.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single market data observation (OHLCV).
 */
public class Candle {

    private final LocalDateTime timestamp;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final long volume;

    public Candle(
            LocalDateTime timestamp,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            long volume) {

        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.open = Objects.requireNonNull(open, "Open price cannot be null");
        this.high = Objects.requireNonNull(high, "High price cannot be null");
        this.low = Objects.requireNonNull(low, "Low price cannot be null");
        this.close = Objects.requireNonNull(close, "Close price cannot be null");

        if (volume < 0) {
            throw new IllegalArgumentException("Volume cannot be negative: " + volume);
        }
        this.volume = volume;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public long getVolume() {
        return volume;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Candle candle = (Candle) o;
        return volume == candle.volume &&
                Objects.equals(timestamp, candle.timestamp) &&
                Objects.equals(open, candle.open) &&
                Objects.equals(high, candle.high) &&
                Objects.equals(low, candle.low) &&
                Objects.equals(close, candle.close);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, open, high, low, close, volume);
    }

    @Override
    public String toString() {
        return "Candle{" +
                "timestamp=" + timestamp +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                '}';
    }
}
