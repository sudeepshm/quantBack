package com.quantback.order;

import com.quantback.data.Candle;
import com.quantback.portfolio.Portfolio;
import com.quantback.strategy.Signal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderManagerTest {

    private OrderManager orderManager;
    private Portfolio portfolio;
    private Candle candle;

    @BeforeEach
    void setUp() {
        orderManager = new OrderManager(10);
        portfolio = new Portfolio(new BigDecimal("100000.00"));
        candle = new Candle(
                LocalDateTime.of(2026, 1, 1, 9, 15),
                new BigDecimal("100.00"),
                new BigDecimal("105.00"),
                new BigDecimal("98.00"),
                new BigDecimal("102.00"),
                5000L
        );
    }

    @Test
    @DisplayName("HOLD signal produces no order")
    void testHoldSignal() {
        Optional<Order> order = orderManager.createOrder(Signal.HOLD, "NIFTY", candle, portfolio);
        assertThat(order).isEmpty();
    }

    @Test
    @DisplayName("BUY signal creates BUY order with default quantity when capital is sufficient")
    void testBuySignal() {
        Optional<Order> orderOpt = orderManager.createOrder(Signal.BUY, "NIFTY", candle, portfolio);
        assertThat(orderOpt).isPresent();

        Order order = orderOpt.get();
        assertThat(order.getSymbol()).isEqualTo("NIFTY");
        assertThat(order.getSide()).isEqualTo(OrderSide.BUY);
        assertThat(order.getType()).isEqualTo(OrderType.MARKET);
        assertThat(order.getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("BUY signal does not create order if already holding an open position")
    void testBuySignalAlreadyHolding() {
        // First order
        Optional<Order> firstOrder = orderManager.createOrder(Signal.BUY, "NIFTY", candle, portfolio);
        assertThat(firstOrder).isPresent();

        // Simulate filling order and adding to portfolio
        portfolio.getPosition("NIFTY").ifPresentOrElse(
                p -> p.add(10, new BigDecimal("102.00")),
                () -> {
                    var p = new com.quantback.portfolio.Position("NIFTY");
                    p.add(10, new BigDecimal("102.00"));
                    // directly apply trade to portfolio
                    portfolio.applyTrade(new com.quantback.trade.Trade(
                            "ord-1", "NIFTY", OrderSide.BUY, 10, new BigDecimal("102.00"), BigDecimal.ZERO, candle.getTimestamp()
                    ));
                }
        );

        Optional<Order> secondOrder = orderManager.createOrder(Signal.BUY, "NIFTY", candle, portfolio);
        assertThat(secondOrder).isEmpty();
    }

    @Test
    @DisplayName("SELL signal creates SELL order to close open position")
    void testSellSignalWithOpenPosition() {
        portfolio.applyTrade(new com.quantback.trade.Trade(
                "ord-1", "NIFTY", OrderSide.BUY, 10, new BigDecimal("100.00"), BigDecimal.ZERO, candle.getTimestamp()
        ));

        Optional<Order> sellOrderOpt = orderManager.createOrder(Signal.SELL, "NIFTY", candle, portfolio);
        assertThat(sellOrderOpt).isPresent();
        assertThat(sellOrderOpt.get().getSide()).isEqualTo(OrderSide.SELL);
        assertThat(sellOrderOpt.get().getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("SELL signal creates no order if no position is open")
    void testSellSignalNoPosition() {
        Optional<Order> sellOrderOpt = orderManager.createOrder(Signal.SELL, "NIFTY", candle, portfolio);
        assertThat(sellOrderOpt).isEmpty();
    }

    @Test
    @DisplayName("Triggers Stop-Loss order when price drops below stopLossPercent")
    void testStopLossTrigger() {
        // 5% stop loss
        OrderManager riskManager = new OrderManager(10, 5.0, 0.0, 0.0);
        portfolio.applyTrade(new com.quantback.trade.Trade(
                "buy-1", "NIFTY", OrderSide.BUY, 10, new BigDecimal("100.00"), BigDecimal.ZERO, candle.getTimestamp()
        ));

        // Candle drops to 94.00 (below 95.00 stop level)
        Candle dropCandle = new Candle(
                candle.getTimestamp().plusDays(1),
                new BigDecimal("96.00"),
                new BigDecimal("97.00"),
                new BigDecimal("94.00"),
                new BigDecimal("94.50"),
                1000
        );

        Optional<Order> exitOrder = riskManager.createOrder(Signal.HOLD, "NIFTY", dropCandle, portfolio);
        assertThat(exitOrder).isPresent();
        assertThat(exitOrder.get().getType()).isEqualTo(OrderType.STOP_LOSS);
        assertThat(exitOrder.get().getSide()).isEqualTo(OrderSide.SELL);
    }

    @Test
    @DisplayName("Triggers Take-Profit order when price hits takeProfitPercent")
    void testTakeProfitTrigger() {
        // 10% take profit
        OrderManager riskManager = new OrderManager(10, 0.0, 10.0, 0.0);
        portfolio.applyTrade(new com.quantback.trade.Trade(
                "buy-1", "NIFTY", OrderSide.BUY, 10, new BigDecimal("100.00"), BigDecimal.ZERO, candle.getTimestamp()
        ));

        // Candle rallies to 112.00 (above 110.00 target)
        Candle rallyCandle = new Candle(
                candle.getTimestamp().plusDays(1),
                new BigDecimal("108.00"),
                new BigDecimal("112.00"),
                new BigDecimal("107.00"),
                new BigDecimal("111.00"),
                1000
        );

        Optional<Order> exitOrder = riskManager.createOrder(Signal.HOLD, "NIFTY", rallyCandle, portfolio);
        assertThat(exitOrder).isPresent();
        assertThat(exitOrder.get().getType()).isEqualTo(OrderType.TAKE_PROFIT);
        assertThat(exitOrder.get().getSide()).isEqualTo(OrderSide.SELL);
    }

    @Test
    @DisplayName("Triggers Trailing-Stop order after rally reverses")
    void testTrailingStopTrigger() {
        // 5% trailing stop
        OrderManager riskManager = new OrderManager(10, 0.0, 0.0, 5.0);
        portfolio.applyTrade(new com.quantback.trade.Trade(
                "buy-1", "NIFTY", OrderSide.BUY, 10, new BigDecimal("100.00"), BigDecimal.ZERO, candle.getTimestamp()
        ));

        // Day 1: Rallies to 120 (peak is now 120, trailing stop is 120 * 0.95 = 114)
        Candle peakCandle = new Candle(
                candle.getTimestamp().plusDays(1),
                new BigDecimal("116.00"),
                new BigDecimal("120.00"),
                new BigDecimal("116.00"),
                new BigDecimal("118.00"),
                1000
        );
        Optional<Order> order1 = riskManager.createOrder(Signal.HOLD, "NIFTY", peakCandle, portfolio);
        assertThat(order1).isEmpty();

        // Day 2: Reverses down to 112 (drops below 114 trail level)
        Candle reversalCandle = new Candle(
                candle.getTimestamp().plusDays(2),
                new BigDecimal("116.00"),
                new BigDecimal("116.00"),
                new BigDecimal("112.00"),
                new BigDecimal("113.00"),
                1000
        );
        Optional<Order> order2 = riskManager.createOrder(Signal.HOLD, "NIFTY", reversalCandle, portfolio);
        assertThat(order2).isPresent();
        assertThat(order2.get().getType()).isEqualTo(OrderType.TRAILING_STOP);
    }
}

