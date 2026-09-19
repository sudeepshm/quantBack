package com.quantback.portfolio;

import com.quantback.order.OrderSide;
import com.quantback.trade.Trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages the financial state of a trading account during a backtest,
 * tracking cash, holdings, executed trades, and equity history.
 */
public class Portfolio {

    private final BigDecimal initialCapital;
    private BigDecimal cash;
    private final Map<String, Position> positions;
    private final List<Trade> trades;
    private final List<PortfolioSnapshot> snapshots;
    private BigDecimal realizedPnl;

    public Portfolio(BigDecimal initialCapital) {
        Objects.requireNonNull(initialCapital, "Initial capital cannot be null");
        if (initialCapital.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Initial capital must be positive: " + initialCapital);
        }
        this.initialCapital = initialCapital;
        this.cash = initialCapital;
        this.positions = new HashMap<>();
        this.trades = new ArrayList<>();
        this.snapshots = new ArrayList<>();
        this.realizedPnl = BigDecimal.ZERO;
    }

    /**
     * Applies an executed trade to the portfolio, updating cash and positions.
     *
     * @param trade the executed trade
     */
    public void applyTrade(Trade trade) {
        Objects.requireNonNull(trade, "Trade cannot be null");

        if (trade.getSide() == OrderSide.BUY) {
            BigDecimal totalCost = trade.getGrossValue().add(trade.getFee());
            if (cash.compareTo(totalCost) < 0) {
                throw new IllegalStateException(
                        String.format("Insufficient cash to execute BUY trade. Required: %s, Available: %s", totalCost, cash)
                );
            }
            cash = cash.subtract(totalCost);
            Position position = positions.computeIfAbsent(trade.getSymbol(), Position::new);
            position.add(trade.getQuantity(), trade.getPrice());
        } else if (trade.getSide() == OrderSide.SELL) {
            Position position = positions.get(trade.getSymbol());
            if (position == null || position.getQuantity() < trade.getQuantity()) {
                int heldQty = position == null ? 0 : position.getQuantity();
                throw new IllegalStateException(
                        String.format("Cannot execute SELL trade for %d units of %s. Currently holding: %d units",
                                trade.getQuantity(), trade.getSymbol(), heldQty)
                );
            }

            BigDecimal proceeds = trade.getGrossValue().subtract(trade.getFee());
            cash = cash.add(proceeds);

            BigDecimal tradePnl = trade.getPrice().subtract(position.getAverageEntryPrice())
                    .multiply(BigDecimal.valueOf(trade.getQuantity()))
                    .subtract(trade.getFee());
            realizedPnl = realizedPnl.add(tradePnl);

            position.reduce(trade.getQuantity());
            if (!position.isOpen()) {
                positions.remove(trade.getSymbol());
            }
        }

        trades.add(trade);
    }

    /**
     * Records a snapshot of portfolio value for multiple symbols.
     */
    public void recordSnapshot(LocalDateTime timestamp, Map<String, BigDecimal> currentPrices) {
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        Objects.requireNonNull(currentPrices, "Current prices cannot be null");

        BigDecimal positionsValue = BigDecimal.ZERO;
        BigDecimal unrealizedPnl = BigDecimal.ZERO;

        for (Position position : positions.values()) {
            BigDecimal price = currentPrices.get(position.getSymbol());
            if (price != null) {
                positionsValue = positionsValue.add(position.getMarketValue(price));
                unrealizedPnl = unrealizedPnl.add(position.getUnrealizedPnl(price));
            }
        }

        BigDecimal totalValue = cash.add(positionsValue);
        snapshots.add(new PortfolioSnapshot(timestamp, cash, positionsValue, totalValue, realizedPnl, unrealizedPnl));
    }

    /**
     * Convenience method to record snapshot for a single symbol.
     */
    public void recordSnapshot(LocalDateTime timestamp, String symbol, BigDecimal currentPrice) {
        recordSnapshot(timestamp, Map.of(symbol, currentPrice));
    }

    public BigDecimal getInitialCapital() {
        return initialCapital;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public Optional<Position> getPosition(String symbol) {
        return Optional.ofNullable(positions.get(symbol));
    }

    public boolean hasOpenPosition(String symbol) {
        Position pos = positions.get(symbol);
        return pos != null && pos.isOpen();
    }

    public Map<String, Position> getPositions() {
        return Collections.unmodifiableMap(positions);
    }

    public List<Trade> getTrades() {
        return Collections.unmodifiableList(trades);
    }

    public List<PortfolioSnapshot> getSnapshots() {
        return Collections.unmodifiableList(snapshots);
    }

    public BigDecimal getRealizedPnl() {
        return realizedPnl;
    }

    public BigDecimal getTotalValue(Map<String, BigDecimal> currentPrices) {
        BigDecimal val = cash;
        for (Position pos : positions.values()) {
            BigDecimal p = currentPrices.get(pos.getSymbol());
            if (p != null) {
                val = val.add(pos.getMarketValue(p));
            }
        }
        return val;
    }
}
