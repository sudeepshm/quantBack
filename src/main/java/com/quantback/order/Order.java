package com.quantback.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an actionable order instruction to be executed by an ExecutionEngine.
 */
public class Order {

    private final String id;
    private final String symbol;
    private final OrderSide side;
    private final OrderType type;
    private final int quantity;
    private final LocalDateTime timestamp;
    private final BigDecimal price;

    public Order(String symbol, OrderSide side, OrderType type, int quantity, LocalDateTime timestamp) {
        this(UUID.randomUUID().toString(), symbol, side, type, quantity, timestamp, null);
    }

    public Order(String symbol, OrderSide side, OrderType type, int quantity, LocalDateTime timestamp, BigDecimal price) {
        this(UUID.randomUUID().toString(), symbol, side, type, quantity, timestamp, price);
    }

    public Order(String id, String symbol, OrderSide side, OrderType type, int quantity, LocalDateTime timestamp) {
        this(id, symbol, side, type, quantity, timestamp, null);
    }

    public Order(String id, String symbol, OrderSide side, OrderType type, int quantity, LocalDateTime timestamp, BigDecimal price) {
        this.id = Objects.requireNonNull(id, "Order id cannot be null");
        this.symbol = Objects.requireNonNull(symbol, "Symbol cannot be null");
        this.side = Objects.requireNonNull(side, "Order side cannot be null");
        this.type = Objects.requireNonNull(type, "Order type cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("Order quantity must be positive: " + quantity);
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order price must be positive if specified: " + price);
        }
        this.quantity = quantity;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return quantity == order.quantity &&
                Objects.equals(id, order.id) &&
                Objects.equals(symbol, order.symbol) &&
                side == order.side &&
                type == order.type &&
                Objects.equals(timestamp, order.timestamp) &&
                Objects.equals(price, order.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, symbol, side, type, quantity, timestamp, price);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", symbol='" + symbol + '\'' +
                ", side=" + side +
                ", type=" + type +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                (price != null ? ", price=" + price : "") +
                '}';
    }
}
