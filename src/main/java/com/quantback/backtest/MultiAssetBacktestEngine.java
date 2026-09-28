package com.quantback.backtest;

import com.quantback.data.Candle;
import com.quantback.data.MarketData;
import com.quantback.execution.ExecutionEngine;
import com.quantback.execution.SimulatedExecutionEngine;
import com.quantback.metrics.PerformanceMetrics;
import com.quantback.metrics.PerformanceMetricsCalculator;
import com.quantback.order.Order;
import com.quantback.order.OrderManager;
import com.quantback.portfolio.Portfolio;
import com.quantback.strategy.Signal;
import com.quantback.strategy.Strategy;
import com.quantback.strategy.multi.MultiAssetStrategy;
import com.quantback.trade.Trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Chronological backtest engine for multi-asset strategies, rotational systems,
 * and portfolio allocation models.
 * Strictly guarantees zero look-ahead bias across all assets.
 */
public class MultiAssetBacktestEngine {

    private final PerformanceMetricsCalculator metricsCalculator;

    public MultiAssetBacktestEngine() {
        this(new PerformanceMetricsCalculator());
    }

    public MultiAssetBacktestEngine(PerformanceMetricsCalculator metricsCalculator) {
        this.metricsCalculator = Objects.requireNonNull(metricsCalculator, "Metrics calculator cannot be null");
    }

    /**
     * Executes a multi-asset simulation.
     *
     * @param universeData map of symbol -> MarketData
     * @param request multi-asset configuration
     * @return BacktestResult containing comprehensive portfolio performance
     */
    public BacktestResult run(Map<String, MarketData> universeData, MultiAssetBacktestRequest request) {
        Objects.requireNonNull(universeData, "Universe data cannot be null");
        Objects.requireNonNull(request, "Request cannot be null");

        if (universeData.isEmpty()) {
            throw new IllegalArgumentException("Cannot run multi-asset backtest on empty universe");
        }

        // Collect all distinct timestamps across all assets in chronological order
        TreeSet<LocalDateTime> allTimestamps = new TreeSet<>();
        for (MarketData data : universeData.values()) {
            for (Candle c : data.getCandles()) {
                allTimestamps.add(c.getTimestamp());
            }
        }

        Portfolio portfolio = new Portfolio(request.getInitialCapital());
        ExecutionEngine executionEngine = new SimulatedExecutionEngine(
                request.getSlippage(),
                request.getTransactionFee()
        );
        OrderManager orderManager = new OrderManager(request.getOrderQuantity());
        MultiAssetStrategy strategy = request.getStrategy();

        // Map to keep track of current candle index per symbol as we step through time
        Map<String, Integer> currentIndices = new HashMap<>();

        for (LocalDateTime time : allTimestamps) {
            // Apply date filters if configured
            if (request.getStartDate() != null && time.isBefore(request.getStartDate())) {
                continue;
            }
            if (request.getEndDate() != null && time.isAfter(request.getEndDate())) {
                continue;
            }

            // Update current visible index for each symbol up to current timestamp
            Map<String, MarketData> visibleUniverse = new HashMap<>();
            Map<String, Candle> currentCandles = new HashMap<>();
            Map<String, BigDecimal> currentPrices = new HashMap<>();

            for (Map.Entry<String, MarketData> entry : universeData.entrySet()) {
                String sym = entry.getKey();
                MarketData data = entry.getValue();
                List<Candle> candles = data.getCandles();

                int idx = currentIndices.getOrDefault(sym, -1);
                while (idx + 1 < candles.size() && !candles.get(idx + 1).getTimestamp().isAfter(time)) {
                    idx++;
                }
                currentIndices.put(sym, idx);

                if (idx >= 0) {
                    visibleUniverse.put(sym, data.slice(idx));
                    Candle c = candles.get(idx);
                    currentCandles.put(sym, c);
                    currentPrices.put(sym, c.getClose());
                }
            }

            if (visibleUniverse.isEmpty()) {
                continue;
            }

            // 1. Strategy generates signals across the universe
            Map<String, Signal> signals = strategy.generateSignals(visibleUniverse, time);

            // 2. Execute signals for each asset
            for (Map.Entry<String, Signal> sigEntry : signals.entrySet()) {
                String sym = sigEntry.getKey();
                Signal signal = sigEntry.getValue();
                Candle candle = currentCandles.get(sym);

                if (candle == null || signal == Signal.HOLD) {
                    continue;
                }

                // Check order feasibility
                Optional<Order> orderOpt = orderManager.createOrder(signal, sym, candle, portfolio);
                if (orderOpt.isPresent()) {
                    try {
                        Trade trade = executionEngine.execute(orderOpt.get(), candle);
                        portfolio.applyTrade(trade);
                    } catch (IllegalStateException e) {
                        // Insufficient cash or already closed - safely bypass
                    }
                }
            }

            // 3. Record point-in-time snapshot
            portfolio.recordSnapshot(time, currentPrices);
        }

        PerformanceMetrics metrics = metricsCalculator.calculate(portfolio);
        Strategy strategyAdapter = new Strategy() {
            @Override
            public Signal generateSignal(MarketData historicalData) {
                return Signal.HOLD;
            }

            @Override
            public String getName() {
                return request.getStrategy().getName();
            }
        };

        BacktestRequest dummyRequest = new BacktestRequest(
                String.join("+", request.getSymbols()),
                strategyAdapter,
                request.getInitialCapital(),
                request.getSlippage(),
                request.getTransactionFee(),
                request.getOrderQuantity(),
                request.getStartDate(),
                request.getEndDate()
        );

        return new BacktestResult(dummyRequest, portfolio, metrics);
    }
}
