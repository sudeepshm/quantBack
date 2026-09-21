import {
  TrendingUp,
  TrendingDown,
  BarChart3,
  Target,
  ArrowRight,
  Percent,
  Scale,
  Activity,
} from 'lucide-react'
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
      <div className="metrics-panel card">
        <div className="card-title">
          <Activity size={16} />
          Performance Metrics
        </div>
        <div className="metrics-grid">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="metric-skeleton">
              <div className="skeleton skel-label" />
              <div className="skeleton skel-value" />
              <div className="skeleton skel-sub" />
            </div>
          ))}
        </div>
      </div>
    )
  }

  if (!result) return null

  const isProfit = result.totalPnl >= 0
  const metrics = [
    {
      label: 'Total Return',
      value: formatPercent(result.totalReturnPercent),
      colorClass: result.totalReturnPercent >= 0 ? 'positive' : 'negative',
      icon: result.totalReturnPercent >= 0 ? TrendingUp : TrendingDown,
    },
    {
      label: 'Sharpe Ratio',
      value: formatNumber(result.sharpeRatio),
      colorClass: 'accent',
      icon: Scale,
    },
    {
      label: 'Max Drawdown',
      value: formatPercent(-Math.abs(result.maxDrawdownPercent)),
      colorClass: 'negative',
      icon: TrendingDown,
    },
    {
      label: 'Win Rate',
      value: `${formatNumber(result.winRatePercent)}%`,
      colorClass: result.winRatePercent >= 50 ? 'positive' : 'negative',
      icon: Target,
      sub: `${result.winningTrades}W / ${result.losingTrades}L`,
    },
    {
      label: 'Profit Factor',
      value: formatNumber(result.profitFactor),
      colorClass: result.profitFactor >= 1 ? 'positive' : 'negative',
      icon: BarChart3,
    },
    {
      label: 'Total P&L',
      value: formatCurrency(result.totalPnl),
      colorClass: isProfit ? 'positive' : 'negative',
      icon: isProfit ? TrendingUp : TrendingDown,
      sub: `${result.totalTrades} trades`,
    },
  ]

  return (
    <div className="metrics-panel card animate-fade-in">
      <div className="card-title">
        <Activity size={16} />
        Performance Metrics
      </div>

      <div className="metrics-grid">
        {/* Capital summary row */}
        <div className="metrics-capital-row">
          <div className="capital-block">
            <span className="capital-label">Initial</span>
            <span className="capital-value">{formatCurrency(result.initialCapital)}</span>
          </div>
          <ArrowRight size={18} className="capital-arrow" />
          <div className="capital-block">
            <span className="capital-label">Final</span>
            <span className="capital-value">{formatCurrency(result.finalCapital)}</span>
          </div>
          <span className={`capital-delta ${isProfit ? 'positive' : 'negative'}`}>
            {formatPercent(result.totalReturnPercent)}
          </span>
        </div>

        {/* Metric cards */}
        {metrics.map((m) => (
          <div key={m.label} className={`metric-card ${m.colorClass}`}>
            <div className="metric-label">
              <m.icon size={12} />
              {m.label}
            </div>
            <div className={`metric-value ${m.colorClass}`}>{m.value}</div>
            {m.sub && <div className="metric-sub">{m.sub}</div>}
          </div>
        ))}
      </div>
    </div>
  )
}
