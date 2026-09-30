# QuantBack

**QuantBack** is a Java-based quantitative trading strategy backtesting engine.

The purpose of QuantBack is to simulate how a trading strategy would have performed on historical market data without placing real trades.

The system takes:

* Historical market data
* A trading strategy
* Strategy parameters
* Initial capital
* Execution assumptions

and produces:

* Simulated orders
* Simulated trades
* Portfolio history
* Equity curve
* Profit/loss
* Risk metrics
* Trade statistics
* A complete backtest result

---

# 1. Project Objective

The primary objective of QuantBack is to build a clean, modular and extensible backtesting engine in Java.

The project is intended to demonstrate practical knowledge of:

* Java
* Object-oriented programming
* Interfaces and abstraction
* Collections and data structures
* Financial calculations
* Algorithm design
* Software architecture
* Unit testing
* Maven
* REST/API architecture
* Data processing
* Performance analysis

QuantBack is initially a **backtesting-only system**.

It does not place real trades.

---

# 2. Core Problem

A trading strategy can be described as a set of rules that produces trading decisions based on market information.

For example:

```text
If 20-day moving average crosses above
50-day moving average:

    BUY

If 20-day moving average crosses below
50-day moving average:

    SELL
```

Instead of manually checking thousands of historical candles, QuantBack automates the process.

The engine walks through historical market data chronologically and simulates what would have happened.

---

# 3. Core Data Flow

The fundamental QuantBack pipeline is:

```text
Historical Market Data
        │
        ▼
   MarketDataLoader
        │
        ▼
    MarketData
        │
        ▼
   BacktestEngine
        │
        ▼
      Strategy
        │
        ▼
      Signal
        │
        ▼
       Order
        │
        ▼
 ExecutionEngine
        │
        ▼
       Trade
        │
        ▼
     Portfolio
        │
        ├── Position
        ├── Cash
        └── PortfolioSnapshot
        │
        ▼
   BacktestResult
        │
        ▼
 Performance Metrics
        │
        ▼
        UI
```

The system must process historical data in chronological order.

---

# 4. No Look-Ahead Bias

One of the most important requirements of QuantBack is avoiding look-ahead bias.

When processing a candle at time `T`, the strategy must not have access to market information from times after `T`.

Conceptually:

```text
Past                         Future
──────────────────●──────────────────────────►
                  T
                  │
                  └── Strategy can see
                      information available
                      up to T
```

The strategy must not see:

```text
T + 1
T + 2
T + 3
...
```

This is essential for producing meaningful historical simulations.

---

# 5. Technology Stack

Initial technology choices:

```text
Language        Java 21+
Build Tool      Maven
Testing         JUnit 5
Version Control Git
Data Format     CSV initially
```

Potential future technologies:

```text
Backend         Spring Boot
API             REST
Frontend        Web UI
Database        PostgreSQL
Charts          JavaScript charting library
Serialization   Jackson
Logging         SLF4J + Logback
```

The initial version will remain as simple as possible and will not introduce these technologies until they are required.

---

# 6. Package Architecture

The current domain packages are:

```text
com.quantback
│
├── data
├── strategy
├── order
├── execution
├── trade
├── portfolio
├── backtest
└── metrics
```

Each package has a specific responsibility:

* **`data`**: Historical candles, data loaders (CSV), validation.
* **`strategy`**: Strategy interface, signals (`BUY`/`SELL`/`HOLD`), indicators and strategy implementations.
* **`order`**: Order instructions (`OrderSide`, `OrderType`, `OrderManager`).
* **`execution`**: Order execution simulation (`SimulatedExecutionEngine`).
* **`trade`**: Executed trade records.
* **`portfolio`**: Account state, positions, cash, and portfolio snapshots.
* **`backtest`**: Backtest orchestrator (`BacktestEngine`, `BacktestRequest`, `BacktestResult`).
* **`metrics`**: Performance metrics calculations (returns, drawdown, Sharpe, win rate, etc.).

---

# 7. Important Design Principles

* **Single Responsibility**: Each class has one clear, focused purpose.
* **Strategy Independence**: The engine works with any `Strategy` implementation.
* **Execution Independence**: The strategy does not execute trades; simulated execution is handled separately.
* **Zero Look-Ahead Bias**: Chronological iteration strictly feeds historical data up to $T$.
* **Financial Precision**: Uses `BigDecimal` for currency and pricing calculations.
* **Testability**: Every domain unit is thoroughly tested with JUnit 5.

---

# 8. Supported Strategies & Risk Controls

### Built-in Strategies
* **Single-Asset & Quantpedia Strategies**:
  * **Moving Average Crossover**: Golden Cross & Death Cross trend execution.
  * **Asset Class Trend-Following (Quantpedia #5 & #77)**: Mebane Faber (2007) tactical trend filter.
  * **Time Series Momentum - TSMOM (Quantpedia #75)**: Moskowitz, Ooi, Pedersen (2012) 12-month momentum.
  * **52-Week High Breakout (Quantpedia #2)**: George & Hwang (2004) near-high momentum.
  * **Short-Term Reversal Effect (Quantpedia #67)**: Jegadeesh & Lehmann (1990) short-term RSI mean reversion.
  * **Turn of the Month Effect (Quantpedia #78)**: Lakonishok & Smidt (1988) calendar turn-of-month window.
  * **Overnight Sentiment Anomaly (Quantpedia #34 & #48)**: Cooper, Dong, Vogel (2008) open-to-close vs overnight gaps.
  * **Consistent Momentum (Quantpedia #11)**: Grinblatt & Moskowitz (2004) multi-bucket return consistency.
  * **January Barometer Effect (Quantpedia #30 & #31)**: Cooper, McConnell, Ovtchinnikov (2006) annual return barometer.
  * **The Halloween Effect / Sell in May (Quantpedia #49)**: Bouman & Jacobsen (2002) winter vs summer equity seasonality.
  * **Bollinger Bands Mean Reversion (Quantpedia #22 & #63)**: John Bollinger (2001) / Leung & Chong (2003) volatility envelope mean reversion.
  * **Day-of-the-Week / Weekend Effect (Quantpedia #16)**: French (1980) & Gibbons & Hess (1981) intra-week seasonality.
  * **Donchian Channel Breakout (Quantpedia #28)**: Richard Donchian (1960) & Curtis Faith (2007) classic Turtle Trading channel breakout.
  * **MACD Trend Following Crossover (Quantpedia #73)**: Gerald Appel (1979) moving average convergence divergence golden & death cross.
  * **Relative Strength Index (RSI) Reversal (Quantpedia #68)**: J. Welles Wilder Jr. (1978) 14-period smoothed momentum & mean-reversion.
* **Multi-Asset Strategies**:
  * **Dual Momentum (Quantpedia #35)**: Gary Antonacci (2012) relative & absolute momentum asset rotation.
  * **Pairs Trading (Quantpedia #51)**: Gatev, Goetzmann, Rouwenhorst (2006) normalized distance statistical arbitrage.
  * **Sector Momentum (Quantpedia #64)**: Cross-sectional top-quartile sector rotation.

### Risk Controls
* **Stop-Loss (%)**: Automatically liquidates positions when drawdowns breach threshold.
* **Take-Profit (%)**: Locks in profits when gains exceed target levels.
* **Trailing-Stop (%)**: Dynamically tracks peak prices to protect upside while limiting drawdown.