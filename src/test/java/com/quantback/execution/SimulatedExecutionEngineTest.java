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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulatedExecutionEngineTest {

    private final Candle candle = new Candle(
            LocalDateTime.of(2026, 1, 1, 9, 15),
            new BigDecimal("100.00"), // open
            new BigDecimal("105.00"), // high
            new BigDecimal("95.00"),  // low
            new BigDecimal("102.00"), // close
            1000L                     // volume
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
        assertThat(trade.getPrice()).isEqualTo(new BigDecimal("102.20"));
        assertThat(trade.getFee()).isEqualTo(new BigDecimal("15.00"));
    }

    @Test
    @DisplayName("Executes SELL order with slippage deducted from price")
    void testSellExecutionWithSlippageAndFee() {
        SimulatedExecutionEngine engine = new SimulatedExecutionEngine(new BigDecimal("0.50"), new BigDecimal("10.00"));
        Order order = new Order("NIFTY", OrderSide.SELL, OrderType.MARKET, 5, candle.getTimestamp());

        Trade trade = engine.execute(order, candle);

        assertThat(trade.getPrice()).isEqualTo(new BigDecimal("101.50"));
        assertThat(trade.getFee()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Executes with OPEN price model at bar opening price")
    void testExecutionWithOpenPriceModel() {
        SimulatedExecutionEngine engine = SimulatedExecutionEngine.builder()
                .priceModel(ExecutionPriceModel.OPEN)
                .slippagePerUnit(new BigDecimal("0.25"))
                .feePerOrder(new BigDecimal("5.00"))
                .build();

        Order buyOrder = new Order("NIFTY", OrderSide.BUY, OrderType.MARKET, 10, candle.getTimestamp());
        Trade trade = engine.execute(buyOrder, candle);

        // Open is 100.00 + 0.25 slippage = 100.25
        assertThat(trade.getPrice()).isEqualTo(new BigDecimal("100.25"));
    }

    @Test
    @DisplayName("Executes with TYPICAL price model: (High + Low + Close) / 3")
    void testExecutionWithTypicalPriceModel() {
        SimulatedExecutionEngine engine = SimulatedExecutionEngine.builder()
                .priceModel(ExecutionPriceModel.TYPICAL)
                .slippagePerUnit(BigDecimal.ZERO)
                .build();

        Order buyOrder = new Order("NIFTY", OrderSide.BUY, OrderType.MARKET, 10, candle.getTimestamp());
        Trade trade = engine.execute(buyOrder, candle);

        // Typical = (105 + 95 + 102) / 3 = 302 / 3 = 100.6667
        assertThat(trade.getPrice()).isEqualByComparingTo(new BigDecimal("100.6667"));
    }

    @Test
    @DisplayName("Executes with PERCENTAGE slippage model (basis points)")
    void testExecutionWithPercentageSlippage() {
        SimulatedExecutionEngine engine = SimulatedExecutionEngine.builder()
                .slippageModel(SlippageModel.PERCENTAGE)
                .slippagePercent(0.10) // 0.10% on close 102.00 = 0.1020
                .build();

        Order buyOrder = new Order("NIFTY", OrderSide.BUY, OrderType.MARKET, 10, candle.getTimestamp());
        Trade trade = engine.execute(buyOrder, candle);

        // 102.00 + 0.1020 = 102.1020
        assertThat(trade.getPrice()).isEqualByComparingTo(new BigDecimal("102.1020"));
    }

    @Test
    @DisplayName("Executes with VOLUME_SHARE market impact slippage model")
    void testExecutionWithVolumeShareSlippage() {
        // volume = 1000, quantity = 50, impact factor = 0.2
        // participation = 50 / 1000 = 0.05
        // impact = 102.00 * 0.2 * 0.05 = 102.00 * 0.01 = 1.0200
        SimulatedExecutionEngine engine = SimulatedExecutionEngine.builder()
                .slippageModel(SlippageModel.VOLUME_SHARE)
                .volumeImpactFactor(0.2)
                .build();

        Order buyOrder = new Order("NIFTY", OrderSide.BUY, OrderType.MARKET, 50, candle.getTimestamp());
        Trade trade = engine.execute(buyOrder, candle);

        assertThat(trade.getPrice()).isEqualByComparingTo(new BigDecimal("103.0200"));
    }

    @Test
    @DisplayName("Fills BUY LIMIT order when market trades down to or below limit price")
    void testLimitBuyOrderFill() {
        SimulatedExecutionEngine engine = SimulatedExecutionEngine.builder()
                .priceModel(ExecutionPriceModel.LIMIT)
                .slippagePerUnit(BigDecimal.ZERO)
                .build();

        // Candle low is 95.00, limit is 98.00 -> should fill!
        Order limitBuy = new Order("NIFTY", OrderSide.BUY, OrderType.LIMIT, 10, candle.getTimestamp(), new BigDecimal("98.00"));

        assertThat(engine.canExecute(limitBuy, candle)).isTrue();
        Trade trade = engine.execute(limitBuy, candle);
        assertThat(trade.getPrice()).isEqualTo(new BigDecimal("98.00"));
    }

    @Test
    @DisplayName("Rejects BUY LIMIT order when market low never reaches limit price")
    void testLimitBuyOrderRejectedWhenUnreached() {
        SimulatedExecutionEngine engine = new SimulatedExecutionEngine();

        // Candle low is 95.00, limit is 90.00 -> unreached!
        Order limitBuy = new Order("NIFTY", OrderSide.BUY, OrderType.LIMIT, 10, candle.getTimestamp(), new BigDecimal("90.00"));

        assertThat(engine.canExecute(limitBuy, candle)).isFalse();
        assertThatThrownBy(() -> engine.execute(limitBuy, candle))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Execution conditions not met");

        Optional<Trade> optionalTrade = engine.tryExecute(limitBuy, candle);
        assertThat(optionalTrade).isEmpty();
    }

    @Test
    @DisplayName("Fills SELL LIMIT order when market trades up to or above limit price")
    void testLimitSellOrderFill() {
        SimulatedExecutionEngine engine = SimulatedExecutionEngine.builder()
                .priceModel(ExecutionPriceModel.LIMIT)
                .slippagePerUnit(BigDecimal.ZERO)
                .build();

        // Candle high is 105.00, limit is 103.00 -> should fill!
        Order limitSell = new Order("NIFTY", OrderSide.SELL, OrderType.LIMIT, 5, candle.getTimestamp(), new BigDecimal("103.00"));

        assertThat(engine.canExecute(limitSell, candle)).isTrue();
        Trade trade = engine.execute(limitSell, candle);
        assertThat(trade.getPrice()).isEqualTo(new BigDecimal("103.00"));
    }

    @Test
    @DisplayName("Rejects SELL LIMIT order when market high never reaches limit price")
    void testLimitSellOrderRejectedWhenUnreached() {
        SimulatedExecutionEngine engine = new SimulatedExecutionEngine();

        // Candle high is 105.00, limit is 110.00 -> unreached!
        Order limitSell = new Order("NIFTY", OrderSide.SELL, OrderType.LIMIT, 5, candle.getTimestamp(), new BigDecimal("110.00"));

        assertThat(engine.canExecute(limitSell, candle)).isFalse();
        assertThat(engine.tryExecute(limitSell, candle)).isEmpty();
    }

    @Test
    @DisplayName("Volume participation constraint prevents order execution when exceeded")
    void testVolumeParticipationRateConstraint() {
        // volume is 1000, max participation rate is 5% -> max 50 shares
        SimulatedExecutionEngine engine = SimulatedExecutionEngine.builder()
                .maxVolumeParticipationRate(0.05)
                .build();

        Order smallOrder = new Order("NIFTY", OrderSide.BUY, OrderType.MARKET, 40, candle.getTimestamp());
        Order largeOrder = new Order("NIFTY", OrderSide.BUY, OrderType.MARKET, 100, candle.getTimestamp());

        assertThat(engine.canExecute(smallOrder, candle)).isTrue();
        assertThat(engine.canExecute(largeOrder, candle)).isFalse();
    }
}
