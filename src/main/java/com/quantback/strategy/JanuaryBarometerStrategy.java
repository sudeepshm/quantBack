package com.quantback.strategy;

import com.quantback.data.Candle;
import com.quantback.data.MarketData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;

/**
 * Quantpedia #30 & #31: January Barometer / Seasonality Effect
 * Based on Cooper, McConnell, and Ovtchinnikov (2006): "The Other January Effect:
 * Predictable Variation in Common Stock Returns".
 *
 * Rule:
 * - "As goes January, so goes the year."
 * - Measures the net price return during the month of January for the current calendar year.
 * - From February 1 through December 31:
 *   - If January return > 0: BUY (bullish regime for remainder of the year).
 *   - If January return <= 0: SELL (bearish / cash regime).
 * - During January itself: HOLD (or accumulate return tracking).
 */
public class JanuaryBarometerStrategy implements Strategy {

    public JanuaryBarometerStrategy() {}

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        if (historicalData.isEmpty()) {
            return Signal.HOLD;
        }

        Candle currentCandle = historicalData.get(historicalData.size() - 1);
        LocalDate currentDate = currentCandle.getTimestamp().toLocalDate();
        int currentYear = currentDate.getYear();

        // If currently in January, observe/hold
        if (currentDate.getMonth() == Month.JANUARY) {
            return Signal.HOLD;
        }

        // Find the first and last trading day in January of currentYear
        BigDecimal janStartPrice = null;
        BigDecimal janEndPrice = null;

        for (int i = 0; i < historicalData.size(); i++) {
            Candle c = historicalData.get(i);
            LocalDate date = c.getTimestamp().toLocalDate();
            if (date.getYear() == currentYear && date.getMonth() == Month.JANUARY) {
                if (janStartPrice == null) {
                    janStartPrice = c.getOpen();
                }
                janEndPrice = c.getClose();
            }
        }

        // If no January data found for this year, default to HOLD
        if (janStartPrice == null || janEndPrice == null) {
            return Signal.HOLD;
        }

        if (janEndPrice.compareTo(janStartPrice) > 0) {
            return Signal.BUY;
        } else {
            return Signal.SELL;
        }
    }

    @Override
    public String getName() {
        return "JanuaryBarometer";
    }
}
