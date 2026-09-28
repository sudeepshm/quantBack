package com.quantback.strategy.multi;

import com.quantback.data.MarketData;
import com.quantback.strategy.Signal;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Strategy abstraction for multi-asset trading, cross-sectional ranking,
 * sector momentum, asset allocation, and pairs trading.
 */
public interface MultiAssetStrategy {

    /**
     * Generates signals for each asset in the universe based on visible historical data up to time T.
     *
     * @param universeData map of symbol -> MarketData containing visible candles up to time T (strictly preventing look-ahead bias)
     * @param currentTimestamp the current point-in-time timestamp
     * @return map of symbol -> Signal (BUY, SELL, HOLD)
     */
    Map<String, Signal> generateSignals(Map<String, MarketData> universeData, LocalDateTime currentTimestamp);

    /**
     * Returns the strategy name.
     */
    String getName();
}
