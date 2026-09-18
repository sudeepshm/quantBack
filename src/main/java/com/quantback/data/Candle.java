package com.quantback.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
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
}
