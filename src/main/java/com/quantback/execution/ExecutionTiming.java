package com.quantback.execution;

/**
 * Defines when an order instruction generated from a strategy signal is executed.
 */
public enum ExecutionTiming {
    /**
     * Executes immediately on the current candle at Close (T).
     */
    CLOSE,

    /**
     * Realistic backtesting: Order generated at candle (T) executes on the next candle (T+1) at Open.
     */
    NEXT_OPEN
}
