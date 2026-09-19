package com.quantback.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents an open holding for a specific financial symbol.
 */
public class Position {

    private final String symbol;
    private int quantity;
    private BigDecimal averageEntryPrice;

    private static final int SCALE = 4;

    public Position(String symbol) {
        this(symbol, 0, BigDecimal.ZERO);
    }

    public Position(String symbol, int quantity, BigDecimal averageEntryPrice) {
        this.symbol = Objects.requireNonNull(symbol, "Symbol cannot be null");
        if (quantity < 0) {
            throw new IllegalArgumentException("Position quantity cannot be negative: " + quantity);
        }
        this.quantity = quantity;
        this.averageEntryPrice = Objects.requireNonNull(averageEntryPrice, "Average entry price cannot be null");
    }

    /**
     * Increases position quantity and recalculates weighted average entry price.
     */
    public void add(int addQuantity, BigDecimal price) {
        if (addQuantity <= 0) {
            throw new IllegalArgumentException("Added quantity must be positive: " + addQuantity);
        }
        Objects.requireNonNull(price, "Price cannot be null");
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive: " + price);
        }

        BigDecimal currentTotal = averageEntryPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal addedTotal = price.multiply(BigDecimal.valueOf(addQuantity));
        int newQuantity = this.quantity + addQuantity;

        this.averageEntryPrice = currentTotal.add(addedTotal).divide(BigDecimal.valueOf(newQuantity), SCALE, RoundingMode.HALF_UP);
        this.quantity = newQuantity;
    }

    /**
     * Reduces position quantity when selling.
     */
    public void reduce(int reduceQuantity) {
        if (reduceQuantity <= 0) {
            throw new IllegalArgumentException("Reduced quantity must be positive: " + reduceQuantity);
        }
        if (reduceQuantity > quantity) {
            throw new IllegalArgumentException(
                    String.format("Cannot reduce %d units from position of %d units", reduceQuantity, quantity)
            );
        }
        this.quantity -= reduceQuantity;
        if (this.quantity == 0) {
            this.averageEntryPrice = BigDecimal.ZERO;
        }
    }

    public boolean isOpen() {
        return quantity > 0;
    }

    public BigDecimal getMarketValue(BigDecimal currentPrice) {
        Objects.requireNonNull(currentPrice, "Current price cannot be null");
        return currentPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getUnrealizedPnl(BigDecimal currentPrice) {
        Objects.requireNonNull(currentPrice, "Current price cannot be null");
        if (!isOpen()) {
            return BigDecimal.ZERO;
        }
        return currentPrice.subtract(averageEntryPrice).multiply(BigDecimal.valueOf(quantity));
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAverageEntryPrice() {
        return averageEntryPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return quantity == position.quantity &&
                Objects.equals(symbol, position.symbol) &&
                Objects.equals(averageEntryPrice, position.averageEntryPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, quantity, averageEntryPrice);
    }

    @Override
    public String toString() {
        return "Position{" +
                "symbol='" + symbol + '\'' +
                ", quantity=" + quantity +
                ", averageEntryPrice=" + averageEntryPrice +
                '}';
    }
}
