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