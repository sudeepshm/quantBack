import { useMemo, useState } from 'react'
import { LineChart, Info } from 'lucide-react'
import './EquityChart.css'

function rng(a) {
  return function () {
    a |= 0
    a = (a + 0x6d2b79f5) | 0
    let t = Math.imul(a ^ (a >>> 15), 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

function gauss(f) {
  return Math.sqrt(-2 * Math.log(f() + 1e-9)) * Math.cos(2 * Math.PI * f())
}

function formatCurrency(val) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(val)
}

export default function EquityChart({ equityCurve, isLoading, symbol }) {
  const [hoverIndex, setHoverIndex] = useState(null)

  // 1. Generate fallback / demo curve if no backtest result yet
  const sampleData = useMemo(() => {
    const f = rng(7)
    const D = 400
    const s = [100000]
    const b = [100000]
    const dates = []
    let baseDate = new Date('2024-01-01')

    for (let i = 0; i < D; i++) {
      const z = gauss(f)
      const z2 = 0.55 * z + 0.83 * gauss(f)
      const rs = 0.0011 + 0.0085 * z
      const rb = 0.0004 + 0.0105 * z2
      s.push(s[i] * (1 + rs))
      b.push(b[i] * (1 + rb))

      const d = new Date(baseDate)
      d.setDate(d.getDate() + i)
      dates.push(d.toISOString().slice(0, 10))
    }
    dates.push(new Date(baseDate.setDate(baseDate.getDate() + D)).toISOString().slice(0, 10))
    return { s, b, dates, isSample: true }
  }, [])

  // 2. Prepare plot data
  const plotData = useMemo(() => {
    if (equityCurve && equityCurve.length > 1) {
      const s = equityCurve.map((d) => d.totalValue)
      const initial = s[0]
      // Benchmark: flat initial capital or cash trajectory
      const b = equityCurve.map(() => initial)
      const dates = equityCurve.map((d) => d.timestamp?.slice(0, 10) || '')
      return { s, b, dates, isSample: false }
    }
    return sampleData
  }, [equityCurve, sampleData])

  const { s, b, dates, isSample } = plotData
  const N = s.length
  const allVals = s.concat(b)
  const mx = Math.max(...allVals)
  const mn = Math.min(...allVals)
  const range = mx - mn || 1

  function getX(i) {
    return ((i / (N - 1)) * 800).toFixed(1)
  }

  function getY(v) {
    return (250 - ((v - mn) / range) * 220).toFixed(1)
  }

  function getPath(arr) {
    return arr
      .map((val, i) => `${i === 0 ? 'M' : 'L'}${getX(i)} ${getY(val)}`)
      .join(' ')
  }

  const pathStrategy = getPath(s)
  const pathBenchmark = getPath(b)

  const activeIndex = hoverIndex !== null ? hoverIndex : N - 1
  const activeVal = s[activeIndex]
  const activeDate = dates[activeIndex]
  const activeX = getX(activeIndex)
  const activeY = getY(activeVal)

  return (
    <div className="box equity-box">
      <div className="equity-header">
        <div>
          <h3 className="d card-title">
            <LineChart size={18} />
            Equity Curve & Benchmark
          </h3>
          <p className="note" style={{ margin: 0 }}>
            {isSample
              ? 'Sample strategy curve simulating daily walk-forward execution.'
              : `Realized equity trajectory for ${symbol || 'strategy'} vs baseline initial capital.`}
          </p>
        </div>

        {activeVal !== undefined && (
          <div className="equity-hover-val">
            <span className="hover-date">{activeDate}</span>
            <span className="hover-price">{formatCurrency(activeVal)}</span>
          </div>
        )}
      </div>

      <div className="svg-container">
        <svg
          viewBox="0 0 800 280"
          role="img"
          aria-label="Strategy equity curve compared with baseline"
          onMouseMove={(e) => {
            const rect = e.currentTarget.getBoundingClientRect()
            const mouseX = Math.max(0, Math.min(e.clientX - rect.left, rect.width))
            const ratio = mouseX / rect.width
            const idx = Math.min(N - 1, Math.max(0, Math.round(ratio * (N - 1))))
            setHoverIndex(idx)
          }}
          onMouseLeave={() => setHoverIndex(null)}
        >
          {/* Grid lines */}
          {[0, 1, 2, 3].map((k) => (
            <line
              key={k}
              x1={0}
              x2={800}
              y1={25 + k * 72}
              y2={25 + k * 72}
              stroke="var(--ink)"
              strokeOpacity="0.12"
              strokeDasharray="4 4"
            />
          ))}

          {/* Benchmark curve */}
          <path
            d={pathBenchmark}
            fill="none"
            stroke="var(--ink)"
            strokeWidth="2.5"
            strokeOpacity="0.45"
            strokeDasharray={isSample ? 'none' : '5 5'}
            strokeLinejoin="round"
            strokeLinecap="round"
          />

          {/* Strategy curve */}
          <path
            d={pathStrategy}
            fill="none"
            stroke="#FF4FA3"
            strokeWidth="4.5"
            strokeLinejoin="round"
            strokeLinecap="round"
          />

          {/* Active pointer marker */}
          {activeX && (
            <>
              <line
                x1={activeX}
                x2={activeX}
                y1={10}
                y2={260}
                stroke="var(--ink)"
                strokeOpacity="0.25"
                strokeWidth="1.5"
              />
              <circle
                cx={activeX}
                cy={activeY}
                r={6}
                fill="#FF4FA3"
                stroke="var(--ink)"
                strokeWidth={2.5}
              />
            </>
          )}
        </svg>
      </div>

      <div className="leg">
        <span>
          <i style={{ background: 'var(--pink)' }} />
          Strategy Portfolio Value
        </span>
        <span>
          <i style={{ background: 'var(--ink)', opacity: 0.45 }} />
          Benchmark Baseline
        </span>
      </div>
    </div>
  )
}
