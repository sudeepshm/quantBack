package com.quantback.execution;

import com.quantback.data.Candle;
import com.quantback.order.Order;
import com.quantback.trade.Trade;

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
}
