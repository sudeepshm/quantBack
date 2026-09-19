package com.quantback.execution;

import com.quantback.data.Candle;
import com.quantback.order.Order;
import com.quantback.order.OrderSide;
import com.quantback.order.OrderType;
import com.quantback.trade.Trade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatedExecutionEngineTest {

    private final Candle candle = new Candle(
            LocalDateTime.of(2026, 1, 1, 9, 15),
            new BigDecimal("100.00"),
            new BigDecimal("105.00"),
            new BigDecimal("98.00"),
            new BigDecimal("100.00"),
            1000L
    );

    @Test
    @DisplayName("Executes BUY order with positive slippage added to price")
    void testBuyExecutionWithSlippageAndFee() {
        SimulatedExecutionEngine engine = new SimulatedExecutionEngine(new BigDecimal("0.20"), new BigDecimal("15.00"));
        Order order = new Order("NIFTY", OrderSide.BUY, OrderType.MARKET, 10, candle.getTimestamp());

        Trade trade = engine.execute(order, candle);

        assertThat(trade.getOrderId()).isEqualTo(order.getId());
        assertThat(trade.getSymbol()).isEqualTo("NIFTY");
        assertThat(trade.getSide()).isEqualTo(OrderSide.BUY);
        assertThat(trade.getQuantity()).isEqualTo(10);
        assertThat(trade.getPrice()).isEqualTo(new BigDecimal("100.20"));
        assertThat(trade.getFee()).isEqualTo(new BigDecimal("15.00"));
    }

    @Test
    @DisplayName("Executes SELL order with slippage deducted from price")
    void testSellExecutionWithSlippageAndFee() {
        SimulatedExecutionEngine engine = new SimulatedExecutionEngine(new BigDecimal("0.50"), new BigDecimal("10.00"));
        Order order = new Order("NIFTY", OrderSide.SELL, OrderType.MARKET, 5, candle.getTimestamp());

        Trade trade = engine.execute(order, candle);

        assertThat(trade.getPrice()).isEqualTo(new BigDecimal("99.50"));
        assertThat(trade.getFee()).isEqualTo(new BigDecimal("10.00"));
    }
}
