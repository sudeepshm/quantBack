import { Activity, ArrowRight } from 'lucide-react'
import './MetricsPanel.css'

function formatCurrency(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(value)
}

function formatPercent(value) {
  const sign = value >= 0 ? '+' : ''
  return `${sign}${value.toFixed(2)}%`
}

function formatNumber(value, decimals = 2) {
  return Number(value).toFixed(decimals)
}

export default function MetricsPanel({ result, isLoading }) {
  if (isLoading) {
    return (
      <div className="box metrics-box">
        <h3 className="d card-title">
          <Activity size={18} />
          Simulation Metrics
        </h3>
        <dl className="m skeleton-m">
          <div className="skeleton-pill"><span>Calculating Sharpe...</span></div>
          <div className="skeleton-pill"><span>Evaluating Drawdown...</span></div>
          <div className="skeleton-pill"><span>Simulating Returns...</span></div>
          <div className="skeleton-pill"><span>Analyzing Trades...</span></div>
        </dl>
      </div>
    )
  }

  if (!result) {
    return (
      <div className="box metrics-box">
        <h3 className="d card-title">
          <Activity size={18} />
          Simulation Metrics
        </h3>
        <p className="note" style={{ marginBottom: 16 }}>
          Run a backtest using the configuration panel to evaluate historical risk and return metrics.
        </p>
        <dl className="m">
          <div><dt>Sharpe Ratio</dt><dd>—</dd></div>
          <div><dt>Max Drawdown</dt><dd>—</dd></div>
          <div><dt>Total Return</dt><dd>—</dd></div>
          <div><dt>Win Rate</dt><dd>—</dd></div>
        </dl>
      </div>
    )
  }

  const isProfit = result.totalPnl >= 0

  return (
    <div className="box metrics-box">
      <div className="metrics-header-row">
        <h3 className="d card-title">
          <Activity size={18} />
          {result.strategy || 'Strategy'} Metrics
        </h3>
        <span className="symbol-pill">{result.symbol}</span>
      </div>

      {/* Capital summary pill */}
      <div className="capital-bar">
        <div className="cap-item">
          <span className="cap-label">Initial</span>
          <span className="cap-val">{formatCurrency(result.initialCapital)}</span>
        </div>
        <ArrowRight size={18} />
        <div className="cap-item">
          <span className="cap-label">Final</span>
          <span className="cap-val">{formatCurrency(result.finalCapital)}</span>
        </div>
        <div className={`cap-pnl ${isProfit ? 'green' : 'red'}`}>
          {formatCurrency(result.totalPnl)} ({formatPercent(result.totalReturnPercent)})
        </div>
      </div>

      {/* The iconic .m metric pill list */}
      <dl className="m">
        <div>
          <dt>Sharpe Ratio</dt>
          <dd>{formatNumber(result.sharpeRatio)}</dd>
        </div>
        <div>
          <dt>Max Drawdown</dt>
          <dd>{formatPercent(-Math.abs(result.maxDrawdownPercent))}</dd>
        </div>
        <div>
          <dt>Total Return</dt>
          <dd>{formatPercent(result.totalReturnPercent)}</dd>
        </div>
        <div>
          <dt>Win Rate ({result.winningTrades}W / {result.losingTrades}L)</dt>
          <dd>{formatNumber(result.winRatePercent)}%</dd>
        </div>
        <div>
          <dt>Profit Factor</dt>
          <dd>{formatNumber(result.profitFactor)}</dd>
        </div>
        <div>
          <dt>Total Trades Executed</dt>
          <dd>{result.totalTrades}</dd>
        </div>
      </dl>
    </div>
  )
}
