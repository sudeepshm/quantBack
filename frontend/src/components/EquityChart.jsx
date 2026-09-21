import { useEffect, useRef, useState } from 'react'
import { createChart, ColorType } from 'lightweight-charts'
import { LineChart } from 'lucide-react'
import './EquityChart.css'

function formatCurrency(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(value)
}

function parseTimestamp(ts) {
  // The backend sends timestamps like "2023-01-02T00:00" or "2023-01-02T09:15"
  // Lightweight Charts needs { year, month, day } or a UNIX timestamp
  const d = new Date(ts)
  return {
    year: d.getFullYear(),
    month: d.getMonth() + 1,
    day: d.getDate(),
  }
}

export default function EquityChart({ equityCurve, isLoading, symbol }) {
  const chartContainerRef = useRef(null)
  const chartRef = useRef(null)
  const [tooltip, setTooltip] = useState(null)

  useEffect(() => {
    if (!equityCurve || equityCurve.length === 0 || !chartContainerRef.current) return

    // Clean up previous chart
    if (chartRef.current) {
      chartRef.current.remove()
      chartRef.current = null
    }

    const container = chartContainerRef.current

    const chart = createChart(container, {
      width: container.clientWidth,
      height: 360,
      layout: {
        background: { type: ColorType.Solid, color: '#0a0e17' },
        textColor: '#64748b',
        fontFamily: "'Inter', sans-serif",
        fontSize: 11,
      },
      grid: {
        vertLines: { color: 'rgba(30, 41, 59, 0.5)' },
        horzLines: { color: 'rgba(30, 41, 59, 0.5)' },
      },
      crosshair: {
        mode: 0,
        vertLine: {
          color: 'rgba(0, 212, 170, 0.3)',
          width: 1,
          style: 2,
          labelBackgroundColor: '#1a2235',
        },
        horzLine: {
          color: 'rgba(0, 212, 170, 0.3)',
          width: 1,
          style: 2,
          labelBackgroundColor: '#1a2235',
        },
      },
      rightPriceScale: {
        borderColor: '#1e293b',
        scaleMargins: { top: 0.1, bottom: 0.05 },
      },
      timeScale: {
        borderColor: '#1e293b',
        timeVisible: false,
      },
      handleScroll: { vertTouchDrag: false },
    })

    chartRef.current = chart

    // Area series for equity curve
    const series = chart.addAreaSeries({
      lineColor: '#00d4aa',
      lineWidth: 2,
      topColor: 'rgba(0, 212, 170, 0.28)',
      bottomColor: 'rgba(0, 212, 170, 0.02)',
      crosshairMarkerBackgroundColor: '#00d4aa',
      crosshairMarkerBorderColor: '#00d4aa',
      crosshairMarkerRadius: 5,
    })

    // Deduplicate by date string to avoid lightweight-charts errors
    const seen = new Set()
    const data = equityCurve
      .map((snap) => {
        const time = parseTimestamp(snap.timestamp)
        const key = `${time.year}-${time.month}-${time.day}`
        if (seen.has(key)) return null
        seen.add(key)
        return { time, value: snap.totalValue }
      })
      .filter(Boolean)

    series.setData(data)
    chart.timeScale().fitContent()

    // Crosshair tooltip
    chart.subscribeCrosshairMove((param) => {
      if (!param.time || !param.seriesData.size) {
        setTooltip(null)
        return
      }

      const price = param.seriesData.get(series)
      if (!price) {
        setTooltip(null)
        return
      }

      // Find matching snapshot for extra detail
      const t = param.time
      const dateStr = `${t.year}-${String(t.month).padStart(2, '0')}-${String(t.day).padStart(2, '0')}`
      const snap = equityCurve.find((s) => s.timestamp.startsWith(dateStr))

      setTooltip({
        date: dateStr,
        value: price.value,
        cash: snap?.cash,
        pnl: snap ? snap.realizedPnl + snap.unrealizedPnl : null,
      })
    })

    // Resize observer
    const resizeObserver = new ResizeObserver((entries) => {
      for (const entry of entries) {
        chart.applyOptions({ width: entry.contentRect.width })
      }
    })
    resizeObserver.observe(container)

    return () => {
      resizeObserver.disconnect()
      chart.remove()
      chartRef.current = null
    }
  }, [equityCurve])

  if (isLoading) {
    return (
      <div className="equity-chart-container card">
        <div className="card-title">
          <div className="equity-chart-title-left">
            <LineChart size={16} />
            Equity Curve
          </div>
        </div>
        <div className="equity-chart-skeleton skeleton" />
      </div>
    )
  }

  if (!equityCurve || equityCurve.length === 0) return null

  const firstVal = equityCurve[0]?.totalValue || 0
  const lastVal = equityCurve[equityCurve.length - 1]?.totalValue || 0
  const changePct = firstVal > 0 ? ((lastVal - firstVal) / firstVal) * 100 : 0

  return (
    <div className="equity-chart-container card">
      <div className="card-title">
        <div className="equity-chart-title-left">
          <LineChart size={16} />
          Equity Curve {symbol && `— ${symbol}`}
        </div>
        <div className="equity-chart-meta">
          <span>
            Peak: <span className="value">{formatCurrency(Math.max(...equityCurve.map((s) => s.totalValue)))}</span>
          </span>
          <span>
            Change:{' '}
            <span className="value" style={{ color: changePct >= 0 ? 'var(--positive)' : 'var(--negative)' }}>
              {changePct >= 0 ? '+' : ''}
              {changePct.toFixed(2)}%
            </span>
          </span>
        </div>
      </div>

      <div className="equity-chart-wrapper" ref={chartContainerRef}>
        {tooltip && (
          <div className="chart-tooltip">
            <div className="chart-tooltip-date">{tooltip.date}</div>
            <div className="chart-tooltip-value">{formatCurrency(tooltip.value)}</div>
            {tooltip.cash != null && (
              <div className="chart-tooltip-detail">Cash: {formatCurrency(tooltip.cash)}</div>
            )}
            {tooltip.pnl != null && (
              <div
                className="chart-tooltip-detail"
                style={{ color: tooltip.pnl >= 0 ? 'var(--positive)' : 'var(--negative)' }}
              >
                P&L: {formatCurrency(tooltip.pnl)}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
