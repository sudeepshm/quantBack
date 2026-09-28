package com.quantback.strategy;

import com.quantback.data.MarketData;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Quantpedia #75: Time Series Momentum Effect (TSMOM)
 * Based on Moskowitz, Ooi, and Pedersen (2012): "Time Series Momentum".
 *
 * Rule:
 * - Computes total return over the past lookback period (e.g., 252 trading days / 12 months).
 * - BUY if return is strictly positive (Close_T > Close_{T - lookback}).
 * - SELL if return is strictly negative (Close_T < Close_{T - lookback}).
 * - HOLD when return is zero or history < lookback.
 */
public class TimeSeriesMomentumStrategy implements Strategy {

    private final int lookbackPeriod;

    public TimeSeriesMomentumStrategy(int lookbackPeriod) {
        if (lookbackPeriod <= 0) {
            throw new IllegalArgumentException("Lookback period must be positive: " + lookbackPeriod);
        }
        this.lookbackPeriod = lookbackPeriod;
    }

    public int getLookbackPeriod() {
        return lookbackPeriod;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        // Need at least lookbackPeriod + 1 candles to compare Close_T with Close_{T - lookback}
        if (historicalData.size() <= lookbackPeriod) {
            return Signal.HOLD;
        }

        int currentIdx = historicalData.size() - 1;
        int pastIdx = currentIdx - lookbackPeriod;

        BigDecimal currentClose = historicalData.get(currentIdx).getClose();
        BigDecimal pastClose = historicalData.get(pastIdx).getClose();

        int cmp = currentClose.compareTo(pastClose);
        if (cmp > 0) {
            return Signal.BUY;
        } else if (cmp < 0) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    @Override
    public String getName() {
        return String.format("TimeSeriesMomentum(%d)", lookbackPeriod);
    }
}
