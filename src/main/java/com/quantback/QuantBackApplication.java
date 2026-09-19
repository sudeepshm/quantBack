package com.quantback;

import com.quantback.backtest.BacktestEngine;
import com.quantback.backtest.BacktestRequest;
import com.quantback.backtest.BacktestResult;
import com.quantback.data.CsvMarketDataLoader;
import com.quantback.data.MarketData;
import com.quantback.strategy.MovingAverageStrategy;
import com.quantback.strategy.Strategy;

import java.io.InputStream;
import java.math.BigDecimal;

/**
 * Main application entry point for running a QuantBack backtest simulation from the console.
 */
public class QuantBackApplication {

    public static void main(String[] args) {
        System.out.println("Initializing QuantBack Engine...");

        try {
            // 1. Load Market Data
            CsvMarketDataLoader loader = new CsvMarketDataLoader();
            String resourcePath = "/data/sample_nifty.csv";
            InputStream in = QuantBackApplication.class.getResourceAsStream(resourcePath);

            if (in == null) {
                System.err.println("Failed to locate sample data resource: " + resourcePath);
                return;
            }

            MarketData marketData = loader.load("NIFTY", in);
            System.out.printf("Loaded %d candles for %s.%n", marketData.size(), marketData.getSymbol());

            // 2. Configure Strategy (Fast SMA = 3, Slow SMA = 8 for 22 sample candles)
            Strategy strategy = new MovingAverageStrategy(3, 8);

            // 3. Configure Backtest Request
            BacktestRequest request = new BacktestRequest(
                    "NIFTY",
                    strategy,
                    new BigDecimal("100000.00"), // Initial Capital: ₹100,000
                    new BigDecimal("0.50"),       // Slippage: ₹0.50 per unit
                    new BigDecimal("20.00"),      // Transaction Fee: ₹20 per trade
                    10,                           // Order Quantity: 10 units
                    null,
                    null
            );

            // 4. Run Backtest
            BacktestEngine engine = new BacktestEngine();
            BacktestResult result = engine.run(marketData, request);

            // 5. Print Output
            result.printSummary();

        } catch (Exception e) {
            System.err.println("Error executing backtest: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
