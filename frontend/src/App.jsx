import { useState, useEffect } from 'react'
import Sidebar from './components/Sidebar'
import BacktestForm from './components/BacktestForm'
import MetricsPanel from './components/MetricsPanel'
import EquityChart from './components/EquityChart'
import TradeLog from './components/TradeLog'
import EmptyState from './components/EmptyState'
import './App.css'

export default function App() {
  const [result, setResult] = useState(null)
  const [isLoading, setIsLoading] = useState(false)
  const [apiStatus, setApiStatus] = useState('checking') // 'checking' | 'connected' | 'offline'
  const [strategies, setStrategies] = useState([])

  // Check API health on mount
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
        // Strategies will fall back to hardcoded default in BacktestForm
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
        symbol: formData.symbol,
        strategy: formData.strategy,
        initialCapital: formData.initialCapital,
        fastPeriod: formData.fastPeriod,
        slowPeriod: formData.slowPeriod,
        orderQuantity: formData.orderQuantity,
        slippagePercent: formData.slippagePercent,
        transactionFeePercent: formData.transactionFeePercent,
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
    } catch (err) {
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="app-layout">
      <Sidebar apiStatus={apiStatus} />

      <main className="app-main">
        <header className="app-header">
          <div>
            <h1>Backtests</h1>
            <p className="app-header-subtitle">
              Configure and run strategy backtests on historical market data
            </p>
          </div>
        </header>

        <div className="app-content">
          {/* Top section: Form + Metrics side by side */}
          <div className="app-top-section">
            <BacktestForm
              onSubmit={handleRunBacktest}
              isLoading={isLoading}
              strategies={strategies}
            />
            <MetricsPanel result={result} isLoading={isLoading} />
          </div>

          {/* Results section */}
          {!result && !isLoading && <EmptyState />}

          {/* Equity chart */}
          <EquityChart
            equityCurve={result?.equityCurve}
            isLoading={isLoading}
            symbol={result?.symbol}
          />

          {/* Trade log */}
          <TradeLog trades={result?.trades} />
        </div>
      </main>
    </div>
  )
}
