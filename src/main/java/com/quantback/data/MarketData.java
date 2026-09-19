package com.quantback.data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an ordered series of candles for a financial instrument.
 */
public class MarketData {

    private final String symbol;
    private final List<Candle> candles;

    public MarketData(String symbol, List<Candle> candles) {
        this.symbol = Objects.requireNonNull(symbol, "Symbol cannot be null");
        Objects.requireNonNull(candles, "Candles list cannot be null");
        this.candles = List.copyOf(candles);
    }

    public String getSymbol() {
        return symbol;
    }

    public List<Candle> getCandles() {
        return candles;
    }

    public Candle get(int index) {
        return candles.get(index);
    }

    public int size() {
        return candles.size();
    }

    public boolean isEmpty() {
        return candles.isEmpty();
    }

    public Candle getLatestCandle() {
        if (candles.isEmpty()) {
            throw new IllegalStateException("Cannot retrieve latest candle from empty MarketData");
        }
        return candles.get(candles.size() - 1);
    }

    /**
     * Slices market data up to the specified index (inclusive),
     * ensuring no future candles are accessible (Zero Look-Ahead Bias).
     *
     * @param toIndexInclusive end index inclusive
     * @return MarketData instance containing candles up to toIndexInclusive
     */
    public MarketData slice(int toIndexInclusive) {
        if (toIndexInclusive < 0 || toIndexInclusive >= candles.size()) {
            throw new IndexOutOfBoundsException("Invalid slice index: " + toIndexInclusive + ", size: " + candles.size());
        }
        return new MarketData(symbol, candles.subList(0, toIndexInclusive + 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketData that = (MarketData) o;
        return Objects.equals(symbol, that.symbol) && Objects.equals(candles, that.candles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, candles);
    }

    @Override
    public String toString() {
        return "MarketData{" +
                "symbol='" + symbol + '\'' +
                ", candleCount=" + candles.size() +
                '}';
    }
}
