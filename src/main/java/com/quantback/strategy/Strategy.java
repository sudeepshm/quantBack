package com.quantback.strategy;

import com.quantback.data.MarketData;

/**
 * Strategy abstraction for generating trading decisions.
 * A strategy receives historical market data available up to the current moment T
 * (preventing look-ahead bias) and outputs a Signal.
 */
public interface Strategy {

    /**
     * Generates a trading signal based on market data up to time T.
     *
     * @param historicalData market data containing candles available up to time T
     * @return Signal (BUY, SELL, HOLD)
     */
    Signal generateSignal(MarketData historicalData);

    /**
     * Returns the human-readable name of the strategy.
     *
     * @return strategy name
     */
    String getName();
}
