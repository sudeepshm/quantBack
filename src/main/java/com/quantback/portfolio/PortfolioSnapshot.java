package com.quantback.portfolio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Records a point-in-time snapshot of the portfolio's financial state.
 */
public class PortfolioSnapshot {

    private final LocalDateTime timestamp;
    private final BigDecimal cash;
    private final BigDecimal positionsValue;
    private final BigDecimal totalValue;
    private final BigDecimal realizedPnl;
    private final BigDecimal unrealizedPnl;

    public PortfolioSnapshot(
            LocalDateTime timestamp,
            BigDecimal cash,
            BigDecimal positionsValue,
            BigDecimal totalValue,
            BigDecimal realizedPnl,
            BigDecimal unrealizedPnl) {

        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.cash = Objects.requireNonNull(cash, "Cash cannot be null");
        this.positionsValue = Objects.requireNonNull(positionsValue, "Positions value cannot be null");
        this.totalValue = Objects.requireNonNull(totalValue, "Total value cannot be null");
        this.realizedPnl = Objects.requireNonNull(realizedPnl, "Realized P&L cannot be null");
        this.unrealizedPnl = Objects.requireNonNull(unrealizedPnl, "Unrealized P&L cannot be null");
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public BigDecimal getPositionsValue() {
        return positionsValue;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public BigDecimal getRealizedPnl() {
        return realizedPnl;
    }

    public BigDecimal getUnrealizedPnl() {
        return unrealizedPnl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PortfolioSnapshot that = (PortfolioSnapshot) o;
        return Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(cash, that.cash) &&
                Objects.equals(positionsValue, that.positionsValue) &&
                Objects.equals(totalValue, that.totalValue) &&
                Objects.equals(realizedPnl, that.realizedPnl) &&
                Objects.equals(unrealizedPnl, that.unrealizedPnl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, cash, positionsValue, totalValue, realizedPnl, unrealizedPnl);
    }

    @Override
    public String toString() {
        return "PortfolioSnapshot{" +
                "timestamp=" + timestamp +
                ", cash=" + cash +
                ", positionsValue=" + positionsValue +
                ", totalValue=" + totalValue +
                ", realizedPnl=" + realizedPnl +
                ", unrealizedPnl=" + unrealizedPnl +
                '}';
    }
}
