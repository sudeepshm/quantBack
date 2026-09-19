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
}
