package com.quantback.trade;

import com.quantback.order.OrderSide;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the executed transaction result of an Order.
 */
public class Trade {

    private final String id;
    private final String orderId;
    private final String symbol;
    private final OrderSide side;
    private final int quantity;
    private final BigDecimal price;
    private final BigDecimal fee;
    private final LocalDateTime timestamp;

    public Trade(
            String orderId,
            String symbol,
            OrderSide side,
            int quantity,
            BigDecimal price,
            BigDecimal fee,
            LocalDateTime timestamp) {
        this(UUID.randomUUID().toString(), orderId, symbol, side, quantity, price, fee, timestamp);
    }

    public Trade(
            String id,
            String orderId,
            String symbol,
            OrderSide side,
            int quantity,
            BigDecimal price,
            BigDecimal fee,
            LocalDateTime timestamp) {

        this.id = Objects.requireNonNull(id, "Trade id cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "Order id cannot be null");
        this.symbol = Objects.requireNonNull(symbol, "Symbol cannot be null");
        this.side = Objects.requireNonNull(side, "Side cannot be null");
        this.price = Objects.requireNonNull(price, "Price cannot be null");
        this.fee = Objects.requireNonNull(fee, "Fee cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive: " + price);
        }
        if (fee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fee cannot be negative: " + fee);
        }

        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getGrossValue() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return quantity == trade.quantity &&
                Objects.equals(id, trade.id) &&
                Objects.equals(orderId, trade.orderId) &&
                Objects.equals(symbol, trade.symbol) &&
                side == trade.side &&
                Objects.equals(price, trade.price) &&
                Objects.equals(fee, trade.fee) &&
                Objects.equals(timestamp, trade.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, symbol, side, quantity, price, fee, timestamp);
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id='" + id + '\'' +
                ", orderId='" + orderId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", side=" + side +
                ", quantity=" + quantity +
                ", price=" + price +
                ", fee=" + fee +
                ", timestamp=" + timestamp +
                '}';
    }
}
