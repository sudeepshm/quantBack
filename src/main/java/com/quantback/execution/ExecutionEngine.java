package com.quantback.execution;

import com.quantback.data.Candle;
import com.quantback.order.Order;
import com.quantback.trade.Trade;

import java.util.Optional;

/**
 * Defines how an order is simulated and executed against current market conditions.
 */
public interface ExecutionEngine {

    /**
     * Executes an order against the provided candle data point.
     *
     * @param order the order to execute
     * @param marketCandle the candle at execution time
     * @return the executed Trade record
     */
    Trade execute(Order order, Candle marketCandle);

    /**
     * Checks if the order can be executed given the candle constraints (e.g., limit price touched, volume limits).
     * Defaults to true for basic execution engines.
     *
     * @param order the order to check
     * @param marketCandle the candle at execution time
     * @return true if execution conditions are satisfied
     */
    default boolean canExecute(Order order, Candle marketCandle) {
        return true;
    }

    /**
     * Attempts to execute an order if execution criteria are met.
     *
     * @param order the order to execute
     * @param marketCandle the candle at execution time
     * @return Optional containing the executed Trade if valid, empty otherwise
     */
    default Optional<Trade> tryExecute(Order order, Candle marketCandle) {
        if (!canExecute(order, marketCandle)) {
            return Optional.empty();
        }
        return Optional.of(execute(order, marketCandle));
    }
}
