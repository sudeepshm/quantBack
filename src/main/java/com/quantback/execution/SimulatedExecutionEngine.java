package com.quantback.execution;

import com.quantback.data.Candle;
import com.quantback.order.Order;
import com.quantback.order.OrderSide;
import com.quantback.order.OrderType;
import com.quantback.trade.Trade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

/**
 * Simulates realistic historical order execution including configurable slippage models
 * (fixed, percentage, volume impact), order price models (close, open, typical, limit),
 * volume participation thresholds, and transaction fees.
 */
public class SimulatedExecutionEngine implements ExecutionEngine {

    private final BigDecimal slippagePerUnit;
    private final BigDecimal feePerOrder;
    private final SlippageModel slippageModel;
    private final ExecutionPriceModel priceModel;
    private final double slippagePercent;
    private final double volumeImpactFactor;
    private final double maxVolumeParticipationRate; // 0.0 means unconstrained

    public SimulatedExecutionEngine() {
        this(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public SimulatedExecutionEngine(BigDecimal slippagePerUnit, BigDecimal feePerOrder) {
        this(slippagePerUnit, feePerOrder, SlippageModel.FIXED_PER_UNIT, ExecutionPriceModel.CLOSE, 0.0, 0.0, 0.0);
    }

    public SimulatedExecutionEngine(BigDecimal slippagePerUnit, BigDecimal feePerOrder,
                                    SlippageModel slippageModel, ExecutionPriceModel priceModel) {
        this(slippagePerUnit, feePerOrder, slippageModel, priceModel, 0.0, 0.0, 0.0);
    }

    public SimulatedExecutionEngine(BigDecimal slippagePerUnit,
                                    BigDecimal feePerOrder,
                                    SlippageModel slippageModel,
                                    ExecutionPriceModel priceModel,
                                    double slippagePercent,
                                    double volumeImpactFactor,
                                    double maxVolumeParticipationRate) {
        this.slippagePerUnit = Objects.requireNonNull(slippagePerUnit, "Slippage cannot be null");
        this.feePerOrder = Objects.requireNonNull(feePerOrder, "Fee cannot be null");
        this.slippageModel = Objects.requireNonNull(slippageModel, "SlippageModel cannot be null");
        this.priceModel = Objects.requireNonNull(priceModel, "PriceModel cannot be null");

        if (slippagePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Slippage cannot be negative: " + slippagePerUnit);
        }
        if (feePerOrder.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fee cannot be negative: " + feePerOrder);
        }
        if (slippagePercent < 0) {
            throw new IllegalArgumentException("Slippage percent cannot be negative: " + slippagePercent);
        }
        if (volumeImpactFactor < 0) {
            throw new IllegalArgumentException("Volume impact factor cannot be negative: " + volumeImpactFactor);
        }
        if (maxVolumeParticipationRate < 0) {
            throw new IllegalArgumentException("Max volume participation rate cannot be negative: " + maxVolumeParticipationRate);
        }

        this.slippagePercent = slippagePercent;
        this.volumeImpactFactor = volumeImpactFactor;
        this.maxVolumeParticipationRate = maxVolumeParticipationRate;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean canExecute(Order order, Candle marketCandle) {
        Objects.requireNonNull(order, "Order cannot be null");
        Objects.requireNonNull(marketCandle, "Market candle cannot be null");

        // 1. Check volume participation constraint if configured
        if (maxVolumeParticipationRate > 0) {
            long maxAllowedQuantity = Math.max(1L, (long) (marketCandle.getVolume() * maxVolumeParticipationRate));
            if (order.getQuantity() > maxAllowedQuantity) {
                return false;
            }
        }

        // 2. Check limit order fill condition against candle range
        if (order.getType() == OrderType.LIMIT && order.getPrice() != null) {
            BigDecimal limitPrice = order.getPrice();
            if (order.getSide() == OrderSide.BUY) {
                // Buy limit fills if market traded at or below limit price
                return marketCandle.getLow().compareTo(limitPrice) <= 0;
            } else {
                // Sell limit fills if market traded at or above limit price
                return marketCandle.getHigh().compareTo(limitPrice) >= 0;
            }
        }

        return true;
    }

    @Override
    public Trade execute(Order order, Candle marketCandle) {
        Objects.requireNonNull(order, "Order cannot be null");
        Objects.requireNonNull(marketCandle, "Market candle cannot be null");

        if (!canExecute(order, marketCandle)) {
            throw new IllegalStateException("Execution conditions not met for order: " + order.getId()
                    + " at " + marketCandle.getTimestamp());
        }

        // 1. Determine base execution reference price
        BigDecimal basePrice = resolveBasePrice(order, marketCandle);

        // 2. Calculate dynamic slippage
        BigDecimal effectiveSlippage = calculateSlippage(basePrice, order, marketCandle);

        // 3. Compute final executed price with directional slippage
        BigDecimal executedPrice;
        if (order.getSide() == OrderSide.BUY) {
            executedPrice = basePrice.add(effectiveSlippage);
        } else {
            executedPrice = basePrice.subtract(effectiveSlippage);
            if (executedPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("Execution price after slippage cannot be non-positive: " + executedPrice);
            }
        }

        return new Trade(
                order.getId(),
                order.getSymbol(),
                order.getSide(),
                order.getType(),
                order.getQuantity(),
                executedPrice,
                feePerOrder,
                marketCandle.getTimestamp()
        );
    }

    private BigDecimal resolveBasePrice(Order order, Candle marketCandle) {
        if (order.getType() == OrderType.LIMIT && order.getPrice() != null) {
            BigDecimal limitPrice = order.getPrice();
            if (order.getSide() == OrderSide.BUY) {
                // Fills at favorable price (min of open and limit)
                return marketCandle.getOpen().min(limitPrice);
            } else {
                // Fills at favorable price (max of open and limit)
                return marketCandle.getOpen().max(limitPrice);
            }
        }

        return switch (priceModel) {
            case OPEN -> marketCandle.getOpen();
            case TYPICAL -> marketCandle.getHigh()
                    .add(marketCandle.getLow())
                    .add(marketCandle.getClose())
                    .divide(BigDecimal.valueOf(3), 4, RoundingMode.HALF_UP);
            case CLOSE, LIMIT -> marketCandle.getClose();
        };
    }

    private BigDecimal calculateSlippage(BigDecimal basePrice, Order order, Candle marketCandle) {
        return switch (slippageModel) {
            case FIXED_PER_UNIT -> slippagePerUnit;
            case PERCENTAGE -> {
                double pct = slippagePercent > 0 ? slippagePercent : (slippagePerUnit.doubleValue() > 0 ? 0.05 : 0.0);
                yield basePrice.multiply(BigDecimal.valueOf(pct / 100.0)).setScale(4, RoundingMode.HALF_UP);
            }
            case VOLUME_SHARE -> {
                double factor = volumeImpactFactor > 0 ? volumeImpactFactor : 0.1;
                long vol = Math.max(1L, marketCandle.getVolume());
                double participation = (double) order.getQuantity() / vol;
                yield basePrice.multiply(BigDecimal.valueOf(factor * participation)).setScale(4, RoundingMode.HALF_UP);
            }
        };
    }

    public BigDecimal getSlippagePerUnit() {
        return slippagePerUnit;
    }

    public BigDecimal getFeePerOrder() {
        return feePerOrder;
    }

    public SlippageModel getSlippageModel() {
        return slippageModel;
    }

    public ExecutionPriceModel getPriceModel() {
        return priceModel;
    }

    public double getSlippagePercent() {
        return slippagePercent;
    }

    public double getVolumeImpactFactor() {
        return volumeImpactFactor;
    }

    public double getMaxVolumeParticipationRate() {
        return maxVolumeParticipationRate;
    }

    /**
     * Fluent Builder for SimulatedExecutionEngine.
     */
    public static class Builder {
        private BigDecimal slippagePerUnit = BigDecimal.ZERO;
        private BigDecimal feePerOrder = BigDecimal.ZERO;
        private SlippageModel slippageModel = SlippageModel.FIXED_PER_UNIT;
        private ExecutionPriceModel priceModel = ExecutionPriceModel.CLOSE;
        private double slippagePercent = 0.0;
        private double volumeImpactFactor = 0.0;
        private double maxVolumeParticipationRate = 0.0;

        public Builder slippagePerUnit(BigDecimal slippage) {
            this.slippagePerUnit = slippage;
            return this;
        }

        public Builder feePerOrder(BigDecimal fee) {
            this.feePerOrder = fee;
            return this;
        }

        public Builder slippageModel(SlippageModel model) {
            this.slippageModel = model;
            return this;
        }

        public Builder priceModel(ExecutionPriceModel model) {
            this.priceModel = model;
            return this;
        }

        public Builder slippagePercent(double percent) {
            this.slippagePercent = percent;
            return this;
        }

        public Builder volumeImpactFactor(double factor) {
            this.volumeImpactFactor = factor;
            return this;
        }

        public Builder maxVolumeParticipationRate(double rate) {
            this.maxVolumeParticipationRate = rate;
            return this;
        }

        public SimulatedExecutionEngine build() {
            return new SimulatedExecutionEngine(
                    slippagePerUnit,
                    feePerOrder,
                    slippageModel,
                    priceModel,
                    slippagePercent,
                    volumeImpactFactor,
                    maxVolumeParticipationRate
            );
        }
    }
}
