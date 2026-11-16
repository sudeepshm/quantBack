package com.quantback.execution;

/**
 * Model defining how market slippage is calculated.
 */
public enum SlippageModel {
    /**
     * Fixed dollar/currency amount per unit (share/contract).
     */
    FIXED_PER_UNIT,

    /**
     * Proportional percentage (basis points) of base price.
     */
    PERCENTAGE,

    /**
     * Volume-weighted market impact based on order size relative to candle volume.
     */
    VOLUME_SHARE
}
