import { useState, useMemo } from 'react'
import {
  BookOpen,
  TrendingUp,
  RotateCcw,
  Calendar,
  Layers,
  ArrowRight,
  Search,
  Sliders,
  CheckCircle2,
  ExternalLink,
  Sparkles,
} from 'lucide-react'
import './StrategyCatalog.css'

export const STRATEGY_CATALOG_DATA = [
  {
    id: 'moving-average',
    name: 'Moving Average Crossover',
    category: 'Trend Following',
    quantpediaId: null,
    citation: 'Technical Analysis Benchmark — Golden Cross & Death Cross',
    description:
      'The foundational trend following model. Buys when a fast moving average crosses above a slow moving average, and exits or reverses when it crosses below.',
    formula: 'Fast SMA(t) > Slow SMA(t) => BUY | Fast SMA(t) < Slow SMA(t) => SELL',
    params: [
      { name: 'fastPeriod', label: 'Fast MA', default: 20 },
      { name: 'slowPeriod', label: 'Slow MA', default: 50 },
    ],
    riskProfile: 'Medium',
    marketRegime: 'Trending Markets',
  },
  {
    id: 'trend-following',
    name: 'Asset Class Trend-Following',
    category: 'Trend Following',
    quantpediaId: '#5 & #77',
    citation: 'Mebane T. Faber (2007) — "A Quantitative Approach to Tactical Asset Allocation"',
    description:
      'Famous 200-day / 10-month simple moving average rule applied across asset classes. Protects against severe market drawdowns by moving 100% to cash when closing below trend.',
    formula: 'Close(t) > SMA_200(t) => LONG | Close(t) <= SMA_200(t) => CASH',
    params: [{ name: 'period', label: 'Trend SMA', default: 200 }],
    riskProfile: 'Low Drawdown',
    marketRegime: 'Long-term Bull Runs',
  },
  {
    id: 'tsmom',
    name: 'Time Series Momentum (TSMOM)',
    category: 'Momentum',
    quantpediaId: '#75',
    citation: 'Moskowitz, Ooi, Pedersen (2012) — "Time Series Momentum", Journal of Financial Economics',
    description:
      'Trend momentum based on past 12-month absolute returns. Holds a long position if the 12-month return is positive, cash otherwise.',
    formula: 'R(t - 252, t) = (Price(t) - Price(t-252)) / Price(t-252) > 0 => LONG',
    params: [{ name: 'lookbackPeriod', label: 'Lookback (bars)', default: 252 }],
    riskProfile: 'Moderate',
    marketRegime: 'Strong Multi-Month Trends',
  },
  {
    id: 'donchian-channel',
    name: 'Donchian Channel Breakout (Turtle Trading)',
    category: 'Trend Following',
    quantpediaId: '#28',
    citation: 'Richard Donchian (1960) & Curtis Faith (2007) — "The Way of the Turtle"',
    description:
      'The legendary Turtle Trading strategy. Enters long when the close price breaks above the 20-day high channel; exits cleanly when dropping below the 10-day low channel.',
    formula: 'Close(t) > Max(High, 20) => BUY | Close(t) < Min(Low, 10) => SELL',
    params: [
      { name: 'entryPeriod', label: 'Entry Channel (High)', default: 20 },
      { name: 'exitPeriod', label: 'Exit Channel (Low)', default: 10 },
    ],
    riskProfile: 'Medium-High (High Win Payoff)',
    marketRegime: 'Explosive Breakouts',
  },
  {
    id: 'macd',
    name: 'MACD Trend Following Crossover',
    category: 'Momentum',
    quantpediaId: '#73',
    citation: 'Gerald Appel (1979) — "Systems and Forecasts"',
    description:
      'Combines fast (12) and slow (26) exponential moving averages to measure momentum velocity. Signals a buy when MACD line crosses above the 9-period Signal line.',
    formula: 'MACD = EMA(12) - EMA(26) | Signal = EMA(MACD, 9) | Bullish Cross => BUY',
    params: [
      { name: 'fastPeriod', label: 'Fast EMA', default: 12 },
      { name: 'slowPeriod', label: 'Slow EMA', default: 26 },
      { name: 'signalPeriod', label: 'Signal EMA', default: 9 },
    ],
    riskProfile: 'Medium',
    marketRegime: 'Momentum Waves',
  },
  {
    id: '52-week-high',
    name: '52-Week High Breakout',
    category: 'Momentum',
    quantpediaId: '#2',
    citation: 'George & Hwang (2004) — "The 52-Week High and Momentum Investing", Journal of Finance',
    description:
      'Exploits investor underreaction to 52-week price peaks. Buys when price trades within 5% of its 52-week high and maintains a trailing buffer for exit.',
    formula: 'Price(t) >= Peak_52W * (1 - threshold%) => BUY | Trailing Drop > buffer% => SELL',
    params: [
      { name: 'lookbackPeriod', label: 'Lookback (bars)', default: 252 },
      { name: 'thresholdPercent', label: 'Near-High Threshold (%)', default: 5.0 },
      { name: 'exitBufferPercent', label: 'Exit Buffer (%)', default: 10.0 },
    ],
    riskProfile: 'Medium',
    marketRegime: 'All-Time High Momentum',
  },
  {
    id: 'consistent-momentum',
    name: 'Consistent Momentum (Frog in the Pan)',
    category: 'Momentum',
    quantpediaId: '#11',
    citation: 'Grinblatt & Moskowitz (2004) — "Predicting Stock Price Movements from Past Returns"',
    description:
      'Separates persistent institutional buying from single-day speculative spikes. Divides lookback into multiple sub-periods and enters only when positive returns are consistent.',
    formula: 'Count(Positive Buckets) / Total Buckets >= 67% => BUY',
    params: [
      { name: 'lookbackPeriod', label: 'Lookback', default: 120 },
      { name: 'numBuckets', label: 'Sub-Buckets', default: 6 },
      { name: 'minConsistencyRatio', label: 'Min Consistency', default: 0.67 },
    ],
    riskProfile: 'Conservative Momentum',
    marketRegime: 'Steady Compounding Trends',
  },
  {
    id: 'short-term-reversal',
    name: 'Short-Term Reversal Effect',
    category: 'Mean Reversion',
    quantpediaId: '#67',
    citation: 'Jegadeesh (1990) & Lehmann (1990) — "Short Term Return Predictability"',
    description:
      'Capitalizes on short-term market overreaction and liquidity provision. Uses 5-period RSI to buy oversold panic sell-offs and exit on mean-reverting bounces.',
    formula: 'RSI(5) <= 30 => BUY Oversold | RSI(5) >= 70 => SELL Overbought',
    params: [
      { name: 'period', label: 'RSI Period', default: 5 },
      { name: 'oversoldThreshold', label: 'Oversold (Buy)', default: 30 },
      { name: 'overboughtThreshold', label: 'Overbought (Sell)', default: 70 },
    ],
    riskProfile: 'High Sharpe, Quick Turnaround',
    marketRegime: 'Choppy & Ranging Markets',
  },
  {
    id: 'rsi',
    name: 'Relative Strength Index (RSI) Reversal',
    category: 'Mean Reversion',
    quantpediaId: '#68',
    citation: 'J. Welles Wilder Jr. (1978) — "New Concepts in Technical Trading Systems"',
    description:
      'Classic Wilder 14-period smoothed Relative Strength Index. Identifies momentum exhaustion and extreme oversold/overbought turning points.',
    formula: 'RSI = 100 - (100 / (1 + AvgGain / AvgLoss)) | RSI <= 30 => BUY | RSI >= 70 => SELL',
    params: [
      { name: 'period', label: 'RSI Period', default: 14 },
      { name: 'oversoldThreshold', label: 'Oversold (Buy)', default: 30 },
      { name: 'overboughtThreshold', label: 'Overbought (Sell)', default: 70 },
    ],
    riskProfile: 'Moderate',
    marketRegime: 'Mean-Reverting Channels',
  },
  {
    id: 'bollinger-bands',
    name: 'Bollinger Bands Mean Reversion',
    category: 'Mean Reversion',
    quantpediaId: '#22 & #63',
    citation: 'John Bollinger (2001) — "Bollinger on Bollinger Bands"; Leung & Chong (2003)',
    description:
      'Dynamically measures volatility expansion. Buys when price dips below the lower 2-standard-deviation band and locks in profits when reverting back to the 20-period SMA.',
    formula: 'Close(t) <= SMA(20) - 2 * sigma => BUY | Close(t) >= SMA(20) => SELL',
    params: [
      { name: 'period', label: 'Period', default: 20 },
      { name: 'stdDevMultiplier', label: 'Std Dev (k)', default: 2.0 },
    ],
    riskProfile: 'Medium',
    marketRegime: 'Volatility Compression & Expansion',
  },
  {
    id: 'turn-of-the-month',
    name: 'Turn of the Month Effect (TOTM)',
    category: 'Seasonality & Calendar',
    quantpediaId: '#78',
    citation: 'Lakonishok & Smidt (1988) — "Turn of the Month in Equity Indexes"',
    description:
      'Exploits persistent end-of-month cash inflows and portfolio rebalancing. Long during the final 4 trading days of the month and first 3 days of the next month.',
    formula: 'Day in [-4 .. +3] of month-boundary => LONG | Otherwise => CASH',
    params: [
      { name: 'daysBeforeMonthEnd', label: 'Days Before End', default: 4 },
      { name: 'daysAfterMonthStart', label: 'Days After Start', default: 3 },
    ],
    riskProfile: 'Low Exposure Time (~30% in market)',
    marketRegime: 'Cash Inflow Seasonality',
  },
  {
    id: 'halloween-effect',
    name: 'The Halloween Effect (Sell in May)',
    category: 'Seasonality & Calendar',
    quantpediaId: '#49',
    citation: 'Bouman & Jacobsen (2002) — "The Halloween Indicator, \'Sell in May and Go Away\'"',
    description:
      'One of the oldest documented market anomalies. Equities systematically outperform from November through April and exhibit higher volatility and lower returns May through October.',
    formula: 'Month in [Nov..Apr] => LONG | Month in [May..Oct] => CASH',
    params: [
      { name: 'entryMonth', label: 'Entry Month (Nov=11)', default: 11 },
      { name: 'exitMonth', label: 'Exit Month (May=5)', default: 5 },
    ],
    riskProfile: 'Broad Seasonality',
    marketRegime: 'Winter Equity Rally',
  },
  {
    id: 'january-barometer',
    name: 'January Barometer Effect',
    category: 'Seasonality & Calendar',
    quantpediaId: '#30 & #31',
    citation: 'Cooper, McConnell, Ovtchinnikov (2006) — "The Other January Effect"',
    description:
      '"As goes January, so goes the year." If the asset generates a positive return during January, it holds long for the remainder of the year; otherwise remains in cash.',
    formula: 'Return(January) > 0 => LONG rest of year | Return(January) <= 0 => CASH',
    params: [],
    riskProfile: 'Low Turnover (1 trade/year)',
    marketRegime: 'Macro Annual Bias',
  },
  {
    id: 'day-of-the-week',
    name: 'Day-of-the-Week / Weekend Effect',
    category: 'Seasonality & Calendar',
    quantpediaId: '#16',
    citation: 'French (1980) & Gibbons & Hess (1981) — "Stock Returns and the Weekend Effect"',
    description:
      'Avoids the statistically negative returns often observed on Mondays. Enters on Tuesday, holds through Friday, and exits before weekend gap risk.',
    formula: 'Day in [Tue..Fri] => LONG | Monday & Weekend => CASH',
    params: [
      { name: 'entryDayOfWeek', label: 'Entry Day (Tue=2)', default: 2 },
      { name: 'exitDayOfWeek', label: 'Exit Day (Fri=5)', default: 5 },
    ],
    riskProfile: 'Low Weekend Gap Risk',
    marketRegime: 'Weekly Flow Seasonality',
  },
  {
    id: 'dual-momentum',
    name: 'Dual Momentum Asset Rotation',
    category: 'Multi-Asset',
    quantpediaId: '#35',
    citation: 'Gary Antonacci (2012) — "Risk Premia Harvesting Through Dual Momentum"',
    description:
      'Combines Relative Momentum (picks the highest performing asset among equities, e.g. NIFTY vs Global) and Absolute Momentum (switches to Cash/Bonds if return < risk-free rate).',
    formula: 'Asset = argmax(R_relative) if R_absolute > 0 else CASH',
    params: [{ name: 'lookbackPeriod', label: 'Lookback', default: 252 }],
    riskProfile: 'Asymmetric Downside Protection',
    marketRegime: 'Cross-Asset Flight to Quality',
  },
  {
    id: 'pairs-trading',
    name: 'Pairs Trading Statistical Arbitrage',
    category: 'Multi-Asset',
    quantpediaId: '#51',
    citation: 'Gatev, Goetzmann, Rouwenhorst (2006) — "Pairs Trading: Performance of a Relative-Value Arbitrage"',
    description:
      'Market-neutral statistical arbitrage between two cointegrated assets. Buys the undervalued asset and sells the overvalued asset when the normalized price ratio diverges.',
    formula: 'Z-Score(Spread) > 2.0 => Short Spread | Z-Score < -2.0 => Long Spread',
    params: [
      { name: 'lookbackPeriod', label: 'Spread Lookback', default: 60 },
      { name: 'entryThreshold', label: 'Entry Z-Score', default: 2.0 },
    ],
    riskProfile: 'Market-Neutral',
    marketRegime: 'Mean-Reverting Asset Pairs',
  },
]

const CATEGORIES = ['All', 'Trend Following', 'Momentum', 'Mean Reversion', 'Seasonality & Calendar', 'Multi-Asset']

export default function StrategyCatalog({ onSelectStrategy }) {
  const [selectedCategory, setSelectedCategory] = useState('All')
  const [searchQuery, setSearchQuery] = useState('')

  const filteredStrategies = useMemo(() => {
    return STRATEGY_CATALOG_DATA.filter((s) => {
      const matchesCategory =
        selectedCategory === 'All' ||
        s.category.toLowerCase().includes(selectedCategory.toLowerCase()) ||
        (selectedCategory === 'Seasonality & Calendar' && s.category.includes('Seasonality'))

      const q = searchQuery.toLowerCase().trim()
      const matchesSearch =
        !q ||
        s.name.toLowerCase().includes(q) ||
        s.description.toLowerCase().includes(q) ||
        s.citation.toLowerCase().includes(q) ||
        (s.quantpediaId && s.quantpediaId.toLowerCase().includes(q))

      return matchesCategory && matchesSearch
    })
  }, [selectedCategory, searchQuery])

  return (
    <div className="strategy-catalog-container">
      {/* Header Banner */}
      <div className="catalog-header-card box">
        <div className="catalog-header-content">
          <div className="catalog-tag">
            <Sparkles size={14} />
            <span>Academic & Institutional Quant Library</span>
          </div>
          <h1 className="catalog-title">Quantitative Strategy Catalog</h1>
          <p className="catalog-subtitle">
            Explore 16 peer-reviewed academic strategies and Quantpedia models implemented with zero look-ahead bias,
            financial precision, and integrated risk management.
          </p>
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="catalog-toolbar box">
        {/* Category Filter Pills */}
        <div className="category-pills">
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              className={`category-pill ${selectedCategory === cat ? 'active' : ''}`}
              onClick={() => setSelectedCategory(cat)}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Search Bar */}
        <div className="catalog-search-wrapper">
          <Search size={16} className="search-icon" />
          <input
            type="text"
            className="catalog-search-input"
            placeholder="Search by strategy name, author, or keyword..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          {searchQuery && (
            <button className="search-clear-btn" onClick={() => setSearchQuery('')}>
              ×
            </button>
          )}
        </div>
      </div>

      {/* Strategy Grid */}
      <div className="strategy-grid">
        {filteredStrategies.map((strategy) => (
          <div key={strategy.id} className="strategy-card box">
            <div className="card-top">
              <div className="card-badges">
                <span className="category-badge">{strategy.category}</span>
                {strategy.quantpediaId && (
                  <span className="quantpedia-badge">
                    Quantpedia {strategy.quantpediaId}
                  </span>
                )}
              </div>
              <h3 className="strategy-card-title">{strategy.name}</h3>
            </div>

            <div className="citation-box">
              <BookOpen size={13} className="citation-icon" />
              <span className="citation-text">{strategy.citation}</span>
            </div>

            <p className="strategy-card-desc">{strategy.description}</p>

            {/* Formula Block */}
            <div className="formula-block">
              <span className="formula-label">Signal Rule:</span>
              <code className="formula-code">{strategy.formula}</code>
            </div>

            {/* Meta Row: Parameters & Regimes */}
            <div className="strategy-meta-row">
              <div className="meta-item">
                <span className="meta-label">Optimal Regime:</span>
                <span className="meta-value">{strategy.marketRegime}</span>
              </div>
              <div className="meta-item">
                <span className="meta-label">Risk Profile:</span>
                <span className="meta-value risk-tag">{strategy.riskProfile}</span>
              </div>
            </div>

            {/* Parameters preview */}
            {strategy.params && strategy.params.length > 0 && (
              <div className="params-preview">
                <span className="params-label">
                  <Sliders size={12} /> Default Parameters:
                </span>
                <div className="param-chips">
                  {strategy.params.map((p) => (
                    <span key={p.name} className="param-chip">
                      {p.label}: <strong>{p.default}</strong>
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Action Footer */}
            <div className="card-action-footer">
              <button
                className="btn-launch-strategy"
                onClick={() => onSelectStrategy(strategy.id)}
              >
                <span>Launch in Backtester</span>
                <ArrowRight size={16} />
              </button>
            </div>
          </div>
        ))}
      </div>

      {filteredStrategies.length === 0 && (
        <div className="empty-catalog box">
          <p className="empty-text">No strategies found matching &quot;{searchQuery}&quot; in {selectedCategory}.</p>
          <button
            className="btn-reset-filters"
            onClick={() => {
              setSelectedCategory('All')
              setSearchQuery('')
            }}
          >
            Reset Filters
          </button>
        </div>
      )}
    </div>
  )
}
