package com.quantback.strategy;

import com.quantback.data.Candle;
import com.quantback.data.MarketData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Quantpedia #34 & #48: Market Sentiment and an Overnight Anomaly
 * Based on Cooper, Dong, and Vogel (2008): "Which Moves Markets: Overnight Returns or Daytime Returns?"
 * and Lou, Polk, and Skouras (2019): "A Tug of War: Overnight Versus Intraday Expected Returns".
 *
 * Rule:
 * - Computes the overnight return between yesterday's close and today's open:
 *   r_overnight = (Open_T - Close_{T-1}) / Close_{T-1}
 * - BUY when overnight return is positive and exceeds the thresholdPercent (bullish overnight sentiment continuation).
 * - SELL when overnight return is negative and falls below -thresholdPercent (bearish sentiment).
 * - HOLD when overnight gap is within the neutral band or data is insufficient.
 */
public class OvernightSentimentStrategy implements Strategy {

    private final double thresholdPercent;
    private static final int SCALE = 6;

    public OvernightSentimentStrategy(double thresholdPercent) {
        if (thresholdPercent < 0) {
            throw new IllegalArgumentException("Threshold percent must be non-negative: " + thresholdPercent);
        }
        this.thresholdPercent = thresholdPercent;
    }

    public double getThresholdPercent() {
        return thresholdPercent;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        // Need at least 2 candles to compute overnight return from T-1 Close to T Open
        if (historicalData.size() < 2) {
            return Signal.HOLD;
        }

        int currentIdx = historicalData.size() - 1;
        Candle currentCandle = historicalData.get(currentIdx);
        Candle prevCandle = historicalData.get(currentIdx - 1);

        BigDecimal prevClose = prevCandle.getClose();
        if (prevClose.compareTo(BigDecimal.ZERO) <= 0) {
            return Signal.HOLD;
        }

        BigDecimal todayOpen = currentCandle.getOpen();
        BigDecimal overnightReturn = todayOpen.subtract(prevClose)
                .divide(prevClose, SCALE, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        BigDecimal threshold = BigDecimal.valueOf(thresholdPercent);

        if (overnightReturn.compareTo(threshold) >= 0) {
            return Signal.BUY;
        } else if (overnightReturn.compareTo(threshold.negate()) <= 0) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    @Override
    public String getName() {
        return String.format("OvernightSentiment(±%.2f%%)", thresholdPercent);
    }
}
