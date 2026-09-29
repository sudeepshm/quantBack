package com.quantback.api.controller;

import com.quantback.api.dto.BacktestRequestDto;
import com.quantback.api.dto.BacktestResultDto;
import com.quantback.api.service.BacktestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST API controller exposing backtest endpoints.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BacktestController {

    private final BacktestService backtestService;
    private final ObjectMapper objectMapper;

    public BacktestController(BacktestService backtestService, ObjectMapper objectMapper) {
        this.backtestService = backtestService;
        this.objectMapper = objectMapper;
    }

    /**
     * POST /api/backtests
     * Accepts multipart form with JSON params and optional CSV file.
     */
    @PostMapping(value = "/backtests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> runBacktest(
            @RequestPart("params") String paramsJson,
            @RequestPart(value = "file", required = false) MultipartFile csvFile) {

        try {
            BacktestRequestDto req = objectMapper.readValue(paramsJson, BacktestRequestDto.class);
            BacktestResultDto result = backtestService.runBacktest(req, csvFile);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/health
     * Simple health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "QuantBack API",
                "version", "1.0"
        ));
    }

    /**
     * GET /api/strategies
     * Returns available strategies and their parameter schemas, including Quantpedia strategies.
     */
    @GetMapping("/strategies")
    public ResponseEntity<?> getStrategies() {
        return ResponseEntity.ok(List.of(
                Map.of(
                        "id", "moving-average",
                        "name", "Moving Average Crossover",
                        "category", "Trend Following",
                        "description", "Buys when fast MA crosses above slow MA (Golden Cross); sells when fast MA crosses below slow MA (Death Cross).",
                        "params", List.of(
                                Map.of("name", "fastPeriod", "label", "Fast MA Period", "default", 20, "min", 1),
                                Map.of("name", "slowPeriod", "label", "Slow MA Period", "default", 50, "min", 2)
                        )
                ),
                Map.of(
                        "id", "trend-following",
                        "name", "Asset Class Trend-Following (Quantpedia #5 & #77)",
                        "category", "Academic / Quantpedia",
                        "citation", "Mebane T. Faber (2007) - A Quantitative Approach to Tactical Asset Allocation",
                        "description", "Goes long when the close price is above the moving average (e.g. 200-day / 10-month SMA); exits to cash when closing below.",
                        "params", List.of(
                                Map.of("name", "period", "label", "Trend SMA Period", "default", 200, "min", 5)
                        )
                ),
                Map.of(
                        "id", "tsmom",
                        "name", "Time Series Momentum - TSMOM (Quantpedia #75)",
                        "category", "Academic / Quantpedia",
                        "citation", "Moskowitz, Ooi, Pedersen (2012) - Time Series Momentum",
                        "description", "Evaluates momentum over lookback period L (e.g. 252 bars / 12 months). Long if return > 0, cash if return < 0.",
                        "params", List.of(
                                Map.of("name", "lookbackPeriod", "label", "Lookback Period (bars)", "default", 252, "min", 5)
                        )
                ),
                Map.of(
                        "id", "52-week-high",
                        "name", "52-Week High Breakout (Quantpedia #2)",
                        "category", "Academic / Quantpedia",
                        "citation", "George & Hwang (2004) - The 52-Week High and Momentum Investing",
                        "description", "Buys when current price is within threshold % of the 52-week peak high; exits when price falls past trailing buffer %.",
                        "params", List.of(
                                Map.of("name", "lookbackPeriod", "label", "High Lookback (bars)", "default", 252, "min", 10),
                                Map.of("name", "thresholdPercent", "label", "Near-High Threshold (%)", "default", 5.0, "min", 0.5),
                                Map.of("name", "exitBufferPercent", "label", "Exit Buffer (%)", "default", 10.0, "min", 1.0)
                        )
                ),
                Map.of(
                        "id", "short-term-reversal",
                        "name", "Short-Term Reversal Effect (Quantpedia #67)",
                        "category", "Academic / Quantpedia",
                        "citation", "Jegadeesh (1990) & Lehmann (1990) - Short Term Reversal in Stocks",
                        "description", "Captures mean-reversion pullbacks using short-term RSI. Buys oversold dips (RSI <= 30), sells on overbought bounces (RSI >= 70).",
                        "params", List.of(
                                Map.of("name", "period", "label", "RSI Period", "default", 5, "min", 2),
                                Map.of("name", "oversoldThreshold", "label", "Oversold Threshold (Buy)", "default", 30.0, "min", 5.0),
                                Map.of("name", "overboughtThreshold", "label", "Overbought Threshold (Sell)", "default", 70.0, "min", 50.0)
                        )
                ),
                Map.of(
                        "id", "turn-of-the-month",
                        "name", "Turn of the Month Effect (Quantpedia #78)",
                        "category", "Academic / Quantpedia",
                        "citation", "Lakonishok & Smidt (1988) - Turn of the Month in Equity Indexes",
                        "description", "Systematically invests during the turn of the calendar month (last few days of the month and first few days of the next month), cash otherwise.",
                        "params", List.of(
                                Map.of("name", "daysBeforeMonthEnd", "label", "Days Before Month End", "default", 4, "min", 1),
                                Map.of("name", "daysAfterMonthStart", "label", "Days After Month Start", "default", 3, "min", 1)
                        )
                ),
                Map.of(
                        "id", "overnight-sentiment",
                        "name", "Overnight Sentiment Anomaly (Quantpedia #34 & #48)",
                        "category", "Academic / Quantpedia",
                        "citation", "Cooper, Dong, Vogel (2008) - Which Moves Markets: Overnight Returns or Daytime Returns?",
                        "description", "Exploits the overnight sentiment gap. Buys when overnight return (Open_T vs Close_{T-1}) exceeds threshold, sells when gap is deeply negative.",
                        "params", List.of(
                                Map.of("name", "thresholdPercent", "label", "Gap Threshold (%)", "default", 0.2, "min", 0.0)
                        )
                ),
                Map.of(
                        "id", "consistent-momentum",
                        "name", "Consistent Momentum Strategy (Quantpedia #11)",
                        "category", "Academic / Quantpedia",
                        "citation", "Grinblatt & Moskowitz (2004) - Predicting Stock Price Movements From Past Returns: The Role of Consistency",
                        "description", "Requires trend consistency across multiple sub-period buckets rather than relying on a single outlier spike.",
                        "params", List.of(
                                Map.of("name", "lookbackPeriod", "label", "Lookback Bars", "default", 120, "min", 20),
                                Map.of("name", "numBuckets", "label", "Number of Buckets", "default", 6, "min", 2),
                                Map.of("name", "minConsistencyRatio", "label", "Min Positive Ratio (e.g. 0.67)", "default", 0.67, "min", 0.1)
                        )
                ),
                Map.of(
                        "id", "january-barometer",
                        "name", "January Barometer Effect (Quantpedia #30 & #31)",
                        "category", "Academic / Quantpedia",
                        "citation", "Cooper, McConnell, Ovtchinnikov (2006) - The Other January Effect",
                        "description", "As goes January, so goes the year. Goes long from Feb-Dec if January net return was positive; exits to cash if negative.",
                        "params", List.of()
                ),
                Map.of(
                        "id", "halloween-effect",
                        "name", "The Halloween Effect / Sell in May (Quantpedia #49)",
                        "category", "Academic / Quantpedia",
                        "citation", "Bouman & Jacobsen (2002) - The Halloween Indicator, 'Sell in May and Go Away'",
                        "description", "Invests in equities during the winter months (November to April) and shifts to cash during summer months (May to October).",
                        "params", List.of(
                                Map.of("name", "entryMonth", "label", "Entry Month (1-12)", "default", 11, "min", 1, "max", 12),
                                Map.of("name", "exitMonth", "label", "Exit Month (1-12)", "default", 5, "min", 1, "max", 12)
                        )
                ),
                Map.of(
                        "id", "bollinger-bands",
                        "name", "Bollinger Bands Mean Reversion (Quantpedia #22 & #63)",
                        "category", "Quantitative / Technical",
                        "citation", "John Bollinger (2001) - Bollinger on Bollinger Bands; Leung & Chong (2003)",
                        "description", "Buys oversold dips when close price breaches below the lower band (SMA - k*sigma); sells when price mean-reverts to SMA.",
                        "params", List.of(
                                Map.of("name", "period", "label", "Bands Period", "default", 20, "min", 2),
                                Map.of("name", "stdDevMultiplier", "label", "Std Dev Multiplier (k)", "default", 2.0, "min", 0.5, "step", 0.5)
                        )
                ),
                Map.of(
                        "id", "day-of-the-week",
                        "name", "Day-of-the-Week / Weekend Effect (Quantpedia #16)",
                        "category", "Academic / Quantpedia",
                        "citation", "French (1980) & Gibbons & Hess (1981) - Stock Returns and the Weekend Effect",
                        "description", "Captures intra-week seasonality. Avoids Monday downside by holding Tuesday through Friday and moving to cash over weekends.",
                        "params", List.of(
                                Map.of("name", "entryDayOfWeek", "label", "Entry Day (1=Mon..7=Sun)", "default", 2, "min", 1, "max", 7),
                                Map.of("name", "exitDayOfWeek", "label", "Exit Day (1=Mon..7=Sun)", "default", 5, "min", 1, "max", 7)
                        )
                )
        ));
    }
}
