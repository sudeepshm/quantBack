package com.quantback.api.service;

import com.quantback.api.dto.BacktestRequestDto;
import com.quantback.api.dto.BacktestResultDto;
import com.quantback.api.dto.BacktestResultDto.SnapshotDto;
import com.quantback.api.dto.BacktestResultDto.TradeDto;
import com.quantback.backtest.BacktestEngine;
import com.quantback.backtest.BacktestRequest;
import com.quantback.backtest.BacktestResult;
import com.quantback.data.CsvMarketDataLoader;
import com.quantback.data.MarketData;
import com.quantback.metrics.PerformanceMetrics;
import com.quantback.portfolio.PortfolioSnapshot;
import com.quantback.strategy.*;
import com.quantback.trade.Trade;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer bridging the REST API with the BacktestEngine.
 */
@Service
public class BacktestService {

    private final BacktestEngine engine = new BacktestEngine();
    private final CsvMarketDataLoader loader = new CsvMarketDataLoader();

    public BacktestResultDto runBacktest(BacktestRequestDto req, MultipartFile csvFile) throws IOException {

        // 1. Load market data
        MarketData marketData;
        String symbol = req.getSymbol() != null && !req.getSymbol().isBlank() ? req.getSymbol().toUpperCase() : "UNKNOWN";

        if (csvFile != null && !csvFile.isEmpty()) {
            marketData = loader.load(symbol, csvFile.getInputStream());
        } else {
            // Fallback to sample data
            var in = getClass().getResourceAsStream("/data/sample_nifty.csv");
            if (in == null) throw new IllegalArgumentException("No CSV file provided and no sample data found");
            marketData = loader.load(symbol, in);
        }

        // 2. Build strategy
        Strategy strategy = buildStrategy(req);

        // 3. Compute absolute slippage/fee from percentage of price
        // Use a representative close price for fee calculation
        BigDecimal representativePrice = marketData.get(marketData.size() / 2).getClose();
        BigDecimal slippage = representativePrice
                .multiply(BigDecimal.valueOf(req.getSlippagePercent() / 100.0));
        BigDecimal fee = representativePrice
                .multiply(BigDecimal.valueOf(req.getTransactionFeePercent() / 100.0));

        // 4. Parse date range
        LocalDateTime startDate = parseDate(req.getStartDate());
        LocalDateTime endDate = parseDate(req.getEndDate());

        // 5. Build and run BacktestRequest
        BacktestRequest backtestRequest = new BacktestRequest(
                symbol,
                strategy,
                BigDecimal.valueOf(req.getInitialCapital()),
                slippage,
                fee,
                req.getOrderQuantity() > 0 ? req.getOrderQuantity() : 10,
                startDate,
                endDate
        );

        BacktestResult result = engine.run(marketData, backtestRequest);
        return toDto(result, strategy.getName());
    }

    private Strategy buildStrategy(BacktestRequestDto req) {
        String strategyName = req.getStrategy() != null ? req.getStrategy().toLowerCase().trim() : "moving-average";
        return switch (strategyName) {
            case "moving-average", "moving_average", "ma-crossover" ->
                    new MovingAverageStrategy(
                            req.getFastPeriod() > 0 ? req.getFastPeriod() : 20,
                            req.getSlowPeriod() > 0 ? req.getSlowPeriod() : 50
                    );
            case "trend-following", "trend_following", "asset-class-trend-following" ->
                    new AssetClassTrendFollowingStrategy(
                            req.getPeriod() > 0 ? req.getPeriod() : 200
                    );
            case "tsmom", "time-series-momentum", "time_series_momentum" ->
                    new TimeSeriesMomentumStrategy(
                            req.getLookbackPeriod() > 0 ? req.getLookbackPeriod() : 252
                    );
            case "52-week-high", "52_week_high", "fifty-two-week-high" ->
                    new FiftyTwoWeekHighStrategy(
                            req.getLookbackPeriod() > 0 ? req.getLookbackPeriod() : 252,
                            req.getThresholdPercent() > 0 ? req.getThresholdPercent() : 5.0,
                            req.getExitBufferPercent() > 0 ? req.getExitBufferPercent() : 10.0
                    );
            case "short-term-reversal", "short_term_reversal", "reversal" ->
                    new ShortTermReversalStrategy(
                            req.getPeriod() > 0 ? req.getPeriod() : 5,
                            req.getOversoldThreshold() > 0 ? req.getOversoldThreshold() : 30.0,
                            req.getOverboughtThreshold() > 0 ? req.getOverboughtThreshold() : 70.0
                    );
            case "turn-of-the-month", "turn_of_the_month", "totm" ->
                    new TurnOfTheMonthStrategy(
                            req.getDaysBeforeMonthEnd() > 0 ? req.getDaysBeforeMonthEnd() : 4,
                            req.getDaysAfterMonthStart() > 0 ? req.getDaysAfterMonthStart() : 3
                    );
            default -> throw new IllegalArgumentException("Unknown strategy: " + req.getStrategy());
        };
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr).atStartOfDay();
    }

    private BacktestResultDto toDto(BacktestResult result, String strategyName) {
        PerformanceMetrics m = result.getMetrics();
        BacktestResultDto dto = new BacktestResultDto();

        dto.setSymbol(result.getRequest().getSymbol());
        dto.setStrategy(strategyName);
        dto.setInitialCapital(m.getInitialCapital().doubleValue());
        dto.setFinalCapital(m.getFinalCapital().doubleValue());
        dto.setTotalPnl(m.getTotalPnl().doubleValue());
        dto.setTotalReturnPercent(m.getTotalReturnPercent());
        dto.setTotalTrades(m.getTotalTrades());
        dto.setWinningTrades(m.getWinningTrades());
        dto.setLosingTrades(m.getLosingTrades());
        dto.setWinRatePercent(m.getWinRatePercent());
        dto.setProfitFactor(m.getProfitFactor());
        dto.setMaxDrawdownPercent(m.getMaxDrawdownPercent());
        dto.setSharpeRatio(m.getSharpeRatio());

        // Trades
        List<TradeDto> tradeDtos = new ArrayList<>();
        for (Trade t : result.getTrades()) {
            TradeDto td = new TradeDto();
            td.setTimestamp(t.getTimestamp().toString());
            td.setSide(t.getSide().name());
            td.setQuantity(t.getQuantity());
            td.setPrice(t.getPrice().doubleValue());
            td.setFee(t.getFee().doubleValue());
            td.setGrossValue(t.getGrossValue().doubleValue());
            tradeDtos.add(td);
        }
        dto.setTrades(tradeDtos);

        // Equity curve
        List<SnapshotDto> snapDtos = new ArrayList<>();
        for (PortfolioSnapshot s : result.getSnapshots()) {
            SnapshotDto sd = new SnapshotDto();
            sd.setTimestamp(s.getTimestamp().toString());
            sd.setTotalValue(s.getTotalValue().doubleValue());
            sd.setCash(s.getCash().doubleValue());
            sd.setPositionsValue(s.getPositionsValue().doubleValue());
            sd.setRealizedPnl(s.getRealizedPnl().doubleValue());
            sd.setUnrealizedPnl(s.getUnrealizedPnl().doubleValue());
            snapDtos.add(sd);
        }
        dto.setEquityCurve(snapDtos);

        return dto;
    }
}
