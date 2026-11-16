package com.quantback.backtest;

import com.quantback.data.Candle;
import com.quantback.data.MarketData;
import com.quantback.execution.ExecutionEngine;
import com.quantback.execution.ExecutionPriceModel;
import com.quantback.execution.ExecutionTiming;
import com.quantback.execution.SimulatedExecutionEngine;
import com.quantback.metrics.PerformanceMetrics;
import com.quantback.metrics.PerformanceMetricsCalculator;
import com.quantback.order.Order;
import com.quantback.order.OrderManager;
import com.quantback.portfolio.Portfolio;
import com.quantback.strategy.Signal;
import com.quantback.strategy.Strategy;
import com.quantback.trade.Trade;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Main orchestrator of the backtesting simulation.
 * Iterates chronologically through market candles, strictly preventing look-ahead bias,
 * generating strategy signals, managing orders, simulating realistic executions (same-bar Close
 * or next-bar Open, limit fills, slippage models), and computing metrics.
 */
public class BacktestEngine {

    private final PerformanceMetricsCalculator metricsCalculator;

    public BacktestEngine() {
        this(new PerformanceMetricsCalculator());
    }

    public BacktestEngine(PerformanceMetricsCalculator metricsCalculator) {
        this.metricsCalculator = Objects.requireNonNull(metricsCalculator, "Metrics calculator cannot be null");
    }

    /**
     * Executes a complete backtest simulation.
     *
     * @param marketData chronological market data
     * @param request configuration parameters for the backtest
     * @return BacktestResult containing portfolio state and performance metrics
     */
    public BacktestResult run(MarketData marketData, BacktestRequest request) {
        Objects.requireNonNull(marketData, "MarketData cannot be null");
        Objects.requireNonNull(request, "BacktestRequest cannot be null");

        if (marketData.isEmpty()) {
            throw new IllegalArgumentException("Cannot run backtest on empty market data");
        }

        // Initialize simulation components
        Portfolio portfolio = new Portfolio(request.getInitialCapital());
        OrderManager orderManager = new OrderManager(
                request.getOrderQuantity(),
                request.getStopLossPercent(),
                request.getTakeProfitPercent(),
                request.getTrailingStopPercent()
        );

        ExecutionPriceModel priceModel = request.getExecutionTiming() == ExecutionTiming.NEXT_OPEN
                ? ExecutionPriceModel.OPEN
                : request.getExecutionPriceModel();

        ExecutionEngine executionEngine = new SimulatedExecutionEngine(
                request.getSlippage(),
                request.getTransactionFee(),
                request.getSlippageModel(),
                priceModel
        );
        Strategy strategy = request.getStrategy();

        List<Candle> candles = marketData.getCandles();
        Order pendingOrder = null;

        // Chronological simulation loop
        for (int i = 0; i < candles.size(); i++) {
            Candle currentCandle = candles.get(i);

            // Filter by date range if specified
            if (request.getStartDate() != null && currentCandle.getTimestamp().isBefore(request.getStartDate())) {
                continue;
            }
            if (request.getEndDate() != null && currentCandle.getTimestamp().isAfter(request.getEndDate())) {
                continue;
            }

            // 0. Next-Bar Open Execution: execute pending order from bar (T-1) on current bar (T) Open
            if (request.getExecutionTiming() == ExecutionTiming.NEXT_OPEN && pendingOrder != null) {
                if (executionEngine.canExecute(pendingOrder, currentCandle)) {
                    Trade trade = executionEngine.execute(pendingOrder, currentCandle);
                    portfolio.applyTrade(trade);
                }
                pendingOrder = null;
            }

            // Zero Look-Ahead Bias: strategy only sees history up to index i
            MarketData visibleHistory = marketData.slice(i);

            // 1. Strategy generates signal
            Signal signal = strategy.generateSignal(visibleHistory);

            // 2. OrderManager creates risk-checked order
            Optional<Order> orderOpt = orderManager.createOrder(
                    signal,
                    marketData.getSymbol(),
                    currentCandle,
                    portfolio
            );

            // 3. Execution handling
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                if (request.getExecutionTiming() == ExecutionTiming.NEXT_OPEN) {
                    // Stage for execution at next candle Open
                    pendingOrder = order;
                } else {
                    // Immediate execution at current candle Close
                    if (executionEngine.canExecute(order, currentCandle)) {
                        Trade trade = executionEngine.execute(order, currentCandle);
                        // 4. Portfolio state updated
                        portfolio.applyTrade(trade);
                    }
                }
            }

            // 5. Record point-in-time snapshot
            portfolio.recordSnapshot(
                    currentCandle.getTimestamp(),
                    marketData.getSymbol(),
                    currentCandle.getClose()
            );
        }

        // Calculate final performance metrics
        PerformanceMetrics metrics = metricsCalculator.calculate(portfolio);

        return new BacktestResult(request, portfolio, metrics);
    }
}
