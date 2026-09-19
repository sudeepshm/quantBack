package com.quantback.metrics;

import com.quantback.order.OrderSide;
import com.quantback.portfolio.Portfolio;
import com.quantback.trade.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceMetricsCalculatorTest {

    private PerformanceMetricsCalculator calculator;
    private Portfolio portfolio;
    private LocalDateTime time;

    @BeforeEach
    void setUp() {
        calculator = new PerformanceMetricsCalculator();
        portfolio = new Portfolio(new BigDecimal("10000.00"));
        time = LocalDateTime.of(2026, 1, 1, 9, 15);
    }

    @Test
    @DisplayName("Calculates zero metrics when no trades occur")
    void testNoTrades() {
        portfolio.recordSnapshot(time, "TEST", new BigDecimal("100.00"));
        PerformanceMetrics metrics = calculator.calculate(portfolio);

        assertThat(metrics.getTotalTrades()).isEqualTo(0);
        assertThat(metrics.getWinningTrades()).isEqualTo(0);
        assertThat(metrics.getLosingTrades()).isEqualTo(0);
        assertThat(metrics.getTotalReturnPercent()).isEqualTo(0.0);
        assertThat(metrics.getMaxDrawdownPercent()).isEqualTo(0.0);
        assertThat(metrics.getWinRatePercent()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Calculates accurate metrics for winning and losing round-trip trades")
    void testTradesWithProfitsAndDrawdowns() {
        // Trade 1: Win
        // Buy 10 @ 100 (cost 1000), Sell 10 @ 120 (proceeds 1200) -> PnL = +200
        portfolio.applyTrade(new Trade("o1", "TEST", OrderSide.BUY, 10, new BigDecimal("100.00"), BigDecimal.ZERO, time));
        portfolio.recordSnapshot(time, "TEST", new BigDecimal("100.00")); // value = 10000

        portfolio.applyTrade(new Trade("o2", "TEST", OrderSide.SELL, 10, new BigDecimal("120.00"), BigDecimal.ZERO, time.plusDays(1)));
        portfolio.recordSnapshot(time.plusDays(1), "TEST", new BigDecimal("120.00")); // value = 10200 (peak)

        // Trade 2: Loss
        // Buy 10 @ 120 (cost 1200), Sell 10 @ 110 (proceeds 1100) -> PnL = -100
        portfolio.applyTrade(new Trade("o3", "TEST", OrderSide.BUY, 10, new BigDecimal("120.00"), BigDecimal.ZERO, time.plusDays(2)));
        portfolio.recordSnapshot(time.plusDays(2), "TEST", new BigDecimal("120.00"));

        portfolio.applyTrade(new Trade("o4", "TEST", OrderSide.SELL, 10, new BigDecimal("110.00"), BigDecimal.ZERO, time.plusDays(3)));
        portfolio.recordSnapshot(time.plusDays(3), "TEST", new BigDecimal("110.00")); // value = 10100

        PerformanceMetrics metrics = calculator.calculate(portfolio);

        assertThat(metrics.getTotalTrades()).isEqualTo(2);
        assertThat(metrics.getWinningTrades()).isEqualTo(1);
        assertThat(metrics.getLosingTrades()).isEqualTo(1);
        assertThat(metrics.getWinRatePercent()).isEqualTo(50.0);
        // Profit factor: Gross profit 200 / Gross loss 100 = 2.0
        assertThat(metrics.getProfitFactor()).isEqualTo(2.0);
        // Initial 10000, final 10100 -> Return = 1%
        assertThat(metrics.getTotalReturnPercent()).isEqualTo(1.0);
        // Peak was 10200, low after peak was 10100 -> Drawdown = (100 / 10200) * 100 = ~0.98%
        assertThat(metrics.getMaxDrawdownPercent()).isGreaterThan(0.0);
    }
}
