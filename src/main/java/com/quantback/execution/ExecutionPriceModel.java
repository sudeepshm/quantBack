package com.quantback.execution;

/**
 * Strategy for determining the base reference price used during order execution.
 */
public enum ExecutionPriceModel {
    /**
     * Executes at the closing price of the candle (standard end-of-bar benchmark).
     */
    CLOSE,

    /**
     * Executes at the opening price of the candle (e.g. for next-bar open execution).
     */
    OPEN,

    /**
     * Executes at the typical price: (High + Low + Close) / 3.
     */
    TYPICAL,

    /**
     * Executes at limit price for limit orders, or market close if unspecified.
     */
    LIMIT
}
