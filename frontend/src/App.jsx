import { useState, useEffect } from 'react'
import { Play, Moon, Sun, ArrowUpRight, BarChart2, ShieldCheck, Zap, Layers } from 'lucide-react'
import ThreeMountain from './components/ThreeMountain'
import BacktestForm from './components/BacktestForm'
import MetricsPanel from './components/MetricsPanel'
import EquityChart from './components/EquityChart'
import TradeLog from './components/TradeLog'
import './App.css'

export default function App() {
  const [result, setResult] = useState(null)
  const [isLoading, setIsLoading] = useState(false)
  const [apiStatus, setApiStatus] = useState('checking') // 'checking' | 'connected' | 'offline'
  const [strategies, setStrategies] = useState([])
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('qb-theme') || 'light'
  })

  // 3D parameter surface readout state
  const [mountainReadout, setMountainReadout] = useState({
    fast: 16,
    slow: 110,
    sharpe: '2.14',
    verdict: 'Optimal Parameter Peak',
  })

  // Set theme on html
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('qb-theme', theme)
  }, [theme])

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'light' ? 'dark' : 'light'))
  }

  // Check API health and load strategy metadata
  useEffect(() => {
    async function checkHealth() {
      try {
        const res = await fetch('/api/health')
        if (res.ok) {
          setApiStatus('connected')
        } else {
          setApiStatus('offline')
        }
      } catch {
        setApiStatus('offline')
      }
    }

    async function loadStrategies() {
      try {
        const res = await fetch('/api/strategies')
        if (res.ok) {
          const data = await res.json()
          setStrategies(data)
        }
      } catch {
        // Handled with fallback options in BacktestForm
      }
    }

    checkHealth()
    loadStrategies()
  }, [])

  const handleRunBacktest = async (formData, csvFile) => {
    setIsLoading(true)
    setResult(null)

    try {
      const params = {
        ...formData,
        startDate: formData.startDate || null,
        endDate: formData.endDate || null,
      }

      const body = new FormData()
      body.append('params', new Blob([JSON.stringify(params)], { type: 'application/json' }))
      if (csvFile) {
        body.append('file', csvFile)
      }

      const res = await fetch('/api/backtests', {
        method: 'POST',
        body,
      })

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}))
        throw new Error(errData.error || `Server error (${res.status})`)
      }

      const data = await res.json()
      setResult(data)

      // Smooth scroll down to view metrics
      setTimeout(() => {
        const resultsEl = document.getElementById('simulation-results')
        if (resultsEl) {
          resultsEl.scrollIntoView({ behavior: 'smooth' })
        }
      }, 100)
    } catch (err) {
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <>
      {/* Interactive 3D parameter surface canvas */}
      <ThreeMountain onReadoutUpdate={setMountainReadout} />

      {/* Fixed Neo-Brutalist Navigation */}
      <nav>
        <div className="wrap">
          <div className="nav-left">
            <a className="logo d" href="#top">
              quantBack
            </a>
            <div className="status-badge">
              <span className={`status-dot ${apiStatus === 'offline' ? 'offline' : ''}`} />
              <span>{apiStatus === 'connected' ? 'API Online' : apiStatus === 'checking' ? 'Connecting' : 'Offline Mode'}</span>
            </div>
          </div>

          <div className="nav-right">
            <button
              type="button"
              className="theme-toggle-btn"
              onClick={toggleTheme}
              aria-label="Toggle theme"
            >
              {theme === 'light' ? <Moon size={16} /> : <Sun size={16} />}
              <span>{theme === 'light' ? 'Dark' : 'Light'}</span>
            </button>
            <a className="btn sm" href="#workspace">
              launch backtest
            </a>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <header className="hero" id="top">
        {/* Floating 3D Parameter Surface Readout */}
        <aside className="read" aria-live="polite">
          <small>Parameter space surface. Hover cursor over the 3D terrain to inspect combinations.</small>
          <dl>
            <dt>fast window</dt>
            <dd id="rf">{mountainReadout.fast}</dd>
            <dt>slow window</dt>
            <dd id="rs">{mountainReadout.slow}</dd>
            <dt className="big" id="rv">
              {mountainReadout.sharpe}
            </dt>
            <dd className="v" id="rd">
              {mountainReadout.verdict}
            </dd>
          </dl>
        </aside>

        <div className="wrap">
          <h1 className="d">
            empirical rigor. <mark>no look-ahead</mark> bias.
          </h1>
          <p className="sub">
            Simulate systematic quantitative trading strategies on historical market data with realistic execution,
            transaction fees, dynamic risk controls, and peer-reviewed Quantpedia alphas.
          </p>
          <div className="hero-actions">
            <a className="btn" href="#workspace">
              <Play size={18} fill="#16081F" />
              run backtest engine
            </a>
            <a className="btn yel" href="#architecture">
              system architecture
            </a>
          </div>
        </div>
      </header>

      {/* Running Marquee Ticker Strip */}
      <div className="strip" aria-hidden="true">
        <div>
          <span>12 quantpedia strategies</span>
          <span>zero look-ahead bias</span>
          <span>realistic slippage & fees</span>
          <span>dynamic trailing-stops</span>
          <span>multi-asset dual momentum</span>
          <span>equity curve reconstruction</span>
          <span>risk-adjusted sharpe</span>
          <span>12 quantpedia strategies</span>
          <span>zero look-ahead bias</span>
          <span>realistic slippage & fees</span>
          <span>dynamic trailing-stops</span>
          <span>multi-asset dual momentum</span>
          <span>equity curve reconstruction</span>
          <span>risk-adjusted sharpe</span>
        </div>
      </div>

      {/* Main Interactive Backtest Workspace */}
      <section className="s" id="workspace">
        <div className="wrap">
          <h2 className="d">execute strategy simulation.</h2>
          <p className="lead">
            Configure your asset symbol, select a quantitative model, fine-tune execution parameters, or upload your own historical dataset.
          </p>

          <div className="workspace-grid">
            <BacktestForm
              onSubmit={handleRunBacktest}
              isLoading={isLoading}
              strategies={strategies}
            />

            <div id="simulation-results" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
              <MetricsPanel result={result} isLoading={isLoading} />
              <EquityChart
                equityCurve={result?.equityCurve}
                isLoading={isLoading}
                symbol={result?.symbol}
              />
            </div>
          </div>

          {result?.trades && result.trades.length > 0 && (
            <TradeLog trades={result.trades} />
          )}
        </div>
      </section>

      {/* Features & Architectural Integrity Section */}
      <section className="s" id="architecture" style={{ paddingTop: 0 }}>
        <div className="wrap">
          <h2 className="d">built for financial precision.</h2>
          <p className="lead">
            Backtests that omit transaction frictions or leak future bars will fail live. QuantBack enforces strict simulation integrity across every step.
          </p>
          <div className="feat">
            <article>
              <h3>zero look-ahead bias</h3>
              Strict chronological single-pass candle iteration guarantees the strategy never accesses future market prices T+1 or T+2.
            </article>
            <article>
              <h3>realistic slippage & fees</h3>
              Brokerage commissions, exchange fees, and percentage-based price impact are charged per trade at execution time, not post-hoc.
            </article>
            <article>
              <h3>dynamic risk controls</h3>
              Integrated Stop-Loss, Take-Profit, and dynamic Trailing-Stops track high-water marks and liquidate positions on adverse moves.
            </article>
            <article>
              <h3>peer-reviewed library</h3>
              Pre-built implementations of Faber Trend Following, Moskowitz TSMOM, George-Hwang 52-Week High, Bouman-Jacobsen Halloween Effect, Bollinger Bands, and more.
            </article>
          </div>
        </div>
      </section>

      {/* Call to Action Banner */}
      <section className="s cta" id="join" style={{ paddingTop: 0 }}>
        <div className="wrap">
          <div className="box">
            <h2 className="d">ready to test your quantitative edge?</h2>
            <p className="lead">
              Run simulations against benchmark indices or benchmark your own custom trading ideas with zero look-ahead bias.
            </p>
            <a className="btn" href="#workspace">
              configure backtest
            </a>
          </div>
        </div>
      </section>

      {/* Neo-Brutalist Footer */}
      <footer>
        <div className="wrap">
          <span>quantBack — Precision Quantitative Trading Strategy Backtesting Engine.</span>
          <span>Historical simulations are for research purposes and do not guarantee future returns.</span>
        </div>
      </footer>
    </>
  )
}
