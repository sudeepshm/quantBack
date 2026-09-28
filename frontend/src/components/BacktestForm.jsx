import { useState, useRef, useEffect } from 'react'
import { Play, Upload, X, AlertCircle, Settings2, BookOpen } from 'lucide-react'
import './BacktestForm.css'

const DEFAULT_STRATEGY_PARAMS = {
  'moving-average': { fastPeriod: 20, slowPeriod: 50 },
  'trend-following': { period: 200 },
  'tsmom': { lookbackPeriod: 252 },
  '52-week-high': { lookbackPeriod: 252, thresholdPercent: 5.0, exitBufferPercent: 10.0 },
  'short-term-reversal': { period: 5, oversoldThreshold: 30, overboughtThreshold: 70 },
  'turn-of-the-month': { daysBeforeMonthEnd: 4, daysAfterMonthStart: 3 },
  'overnight-sentiment': { thresholdPercent: 0.2 },
  'consistent-momentum': { lookbackPeriod: 120, numBuckets: 6, minConsistencyRatio: 0.67 },
  'january-barometer': {},
}

const DEFAULT_VALUES = {
  symbol: 'NIFTY',
  strategy: 'moving-average',
  initialCapital: 100000,
  orderQuantity: 10,
  slippagePercent: 0.05,
  transactionFeePercent: 0.1,
  startDate: '',
  endDate: '',
  ...DEFAULT_STRATEGY_PARAMS['moving-average'],
}

export default function BacktestForm({ onSubmit, isLoading, strategies }) {
  const [form, setForm] = useState(DEFAULT_VALUES)
  const [csvFile, setCsvFile] = useState(null)
  const [error, setError] = useState(null)
  const fileRef = useRef(null)

  // Find strategy params from the strategies list
  const activeStrategy = strategies?.find((s) => s.id === form.strategy)

  const handleStrategyChange = (e) => {
    const strategyId = e.target.value
    const defaultsForStrategy = DEFAULT_STRATEGY_PARAMS[strategyId] || {}
    setForm((prev) => ({
      ...prev,
      strategy: strategyId,
      ...defaultsForStrategy,
    }))
    setError(null)
  }

  const handleChange = (e) => {
    const { name, value, type } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: type === 'number' ? (value === '' ? '' : Number(value)) : value,
    }))
  }

  const handleFileChange = (e) => {
    const file = e.target.files?.[0]
    if (file) setCsvFile(file)
  }

  const removeFile = (e) => {
    e.stopPropagation()
    setCsvFile(null)
    if (fileRef.current) fileRef.current.value = ''
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)

    if (form.initialCapital <= 0) {
      setError('Initial capital must be greater than 0')
      return
    }

    if (form.strategy === 'moving-average' && form.fastPeriod >= form.slowPeriod) {
      setError('Fast MA period must be less than Slow MA period')
      return
    }

    if (form.strategy === '52-week-high' && form.thresholdPercent >= form.exitBufferPercent) {
      setError('Exit buffer % must be strictly greater than near-high threshold %')
      return
    }

    if (form.strategy === 'short-term-reversal' && form.oversoldThreshold >= form.overboughtThreshold) {
      setError('Oversold threshold must be strictly less than overbought threshold')
      return
    }

    try {
      await onSubmit(form, csvFile)
    } catch (err) {
      setError(err.message || 'Failed to run backtest')
    }
  }

  return (
    <form className="backtest-form card" onSubmit={handleSubmit}>
      <div className="card-title">
        <Settings2 size={16} />
        Backtest Configuration
      </div>

      <div className="form-grid">
        {/* Symbol */}
        <div className="form-group">
          <label htmlFor="bf-symbol">Symbol</label>
          <input
            id="bf-symbol"
            name="symbol"
            type="text"
            placeholder="e.g. NIFTY"
            value={form.symbol}
            onChange={handleChange}
          />
        </div>

        {/* Strategy Selector */}
        <div className="form-group">
          <label htmlFor="bf-strategy">Strategy</label>
          <select id="bf-strategy" name="strategy" value={form.strategy} onChange={handleStrategyChange}>
            {strategies && strategies.length > 0 ? (
              strategies.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))
            ) : (
              <>
                <option value="moving-average">Moving Average Crossover</option>
                <option value="trend-following">Asset Class Trend-Following (Quantpedia #5 & #77)</option>
                <option value="tsmom">Time Series Momentum - TSMOM (Quantpedia #75)</option>
                <option value="52-week-high">52-Week High Breakout (Quantpedia #2)</option>
                <option value="short-term-reversal">Short-Term Reversal Effect (Quantpedia #67)</option>
                <option value="turn-of-the-month">Turn of the Month Effect (Quantpedia #78)</option>
              </>
            )}
          </select>
        </div>

        {/* Strategy Info / Academic Citation Card */}
        {activeStrategy && (
          <div className="strategy-info-banner full-width">
            <div className="strategy-info-header">
              <BookOpen size={14} />
              <span>{activeStrategy.category || 'Quantitative Strategy'}</span>
            </div>
            <p className="strategy-info-desc">{activeStrategy.description}</p>
            {activeStrategy.citation && (
              <p className="strategy-info-citation">
                <strong>Source:</strong> {activeStrategy.citation}
              </p>
            )}
          </div>
        )}

        {/* Initial Capital */}
        <div className="form-group">
          <label htmlFor="bf-capital">Initial Capital (₹)</label>
          <input
            id="bf-capital"
            name="initialCapital"
            type="number"
            min="1"
            step="1000"
            value={form.initialCapital}
            onChange={handleChange}
          />
        </div>

        {/* Order Quantity */}
        <div className="form-group">
          <label htmlFor="bf-qty">Order Quantity</label>
          <input
            id="bf-qty"
            name="orderQuantity"
            type="number"
            min="1"
            value={form.orderQuantity}
            onChange={handleChange}
          />
        </div>

        {/* Dynamic Strategy Parameters */}
        {form.strategy === 'moving-average' && (
          <>
            <div className="form-group">
              <label htmlFor="bf-fast">Fast MA Period</label>
              <input
                id="bf-fast"
                name="fastPeriod"
                type="number"
                min="1"
                value={form.fastPeriod ?? 20}
                onChange={handleChange}
              />
            </div>
            <div className="form-group">
              <label htmlFor="bf-slow">Slow MA Period</label>
              <input
                id="bf-slow"
                name="slowPeriod"
                type="number"
                min="2"
                value={form.slowPeriod ?? 50}
                onChange={handleChange}
              />
            </div>
          </>
        )}

        {form.strategy === 'trend-following' && (
          <div className="form-group full-width">
            <label htmlFor="bf-period">Trend SMA Period (e.g. 200)</label>
            <input
              id="bf-period"
              name="period"
              type="number"
              min="2"
              value={form.period ?? 200}
              onChange={handleChange}
            />
          </div>
        )}

        {form.strategy === 'tsmom' && (
          <div className="form-group full-width">
            <label htmlFor="bf-lookback">Lookback Period (bars, e.g. 252 for 1-yr momentum)</label>
            <input
              id="bf-lookback"
              name="lookbackPeriod"
              type="number"
              min="5"
              value={form.lookbackPeriod ?? 252}
              onChange={handleChange}
            />
          </div>
        )}

        {form.strategy === '52-week-high' && (
          <>
            <div className="form-group">
              <label htmlFor="bf-lookback52">High Lookback (bars)</label>
              <input
                id="bf-lookback52"
                name="lookbackPeriod"
                type="number"
                min="10"
                value={form.lookbackPeriod ?? 252}
                onChange={handleChange}
              />
            </div>
            <div className="form-group">
              <label htmlFor="bf-thresh">Near-High Threshold (%)</label>
              <input
                id="bf-thresh"
                name="thresholdPercent"
                type="number"
                step="0.5"
                min="0.5"
                value={form.thresholdPercent ?? 5.0}
                onChange={handleChange}
              />
            </div>
            <div className="form-group full-width">
              <label htmlFor="bf-exitbuf">Exit Buffer (%)</label>
              <input
                id="bf-exitbuf"
                name="exitBufferPercent"
                type="number"
                step="0.5"
                min="1.0"
                value={form.exitBufferPercent ?? 10.0}
                onChange={handleChange}
              />
            </div>
          </>
        )}

        {form.strategy === 'short-term-reversal' && (
          <>
            <div className="form-group">
              <label htmlFor="bf-rsi-period">RSI Period</label>
              <input
                id="bf-rsi-period"
                name="period"
                type="number"
                min="2"
                value={form.period ?? 5}
                onChange={handleChange}
              />
            </div>
            <div className="form-group">
              <label htmlFor="bf-oversold">Oversold Threshold (Buy)</label>
              <input
                id="bf-oversold"
                name="oversoldThreshold"
                type="number"
                step="1"
                min="5"
                max="50"
                value={form.oversoldThreshold ?? 30}
                onChange={handleChange}
              />
            </div>
            <div className="form-group full-width">
              <label htmlFor="bf-overbought">Overbought Threshold (Sell)</label>
              <input
                id="bf-overbought"
                name="overboughtThreshold"
                type="number"
                step="1"
                min="50"
                max="95"
                value={form.overboughtThreshold ?? 70}
                onChange={handleChange}
              />
            </div>
          </>
        )}

        {form.strategy === 'turn-of-the-month' && (
          <>
            <div className="form-group">
              <label htmlFor="bf-days-end">Days Before Month End</label>
              <input
                id="bf-days-end"
                name="daysBeforeMonthEnd"
                type="number"
                min="1"
                value={form.daysBeforeMonthEnd ?? 4}
                onChange={handleChange}
              />
            </div>
            <div className="form-group">
              <label htmlFor="bf-days-start">Days After Month Start</label>
              <input
                id="bf-days-start"
                name="daysAfterMonthStart"
                type="number"
                min="1"
                value={form.daysAfterMonthStart ?? 3}
                onChange={handleChange}
              />
            </div>
          </>
        )}

        {form.strategy === 'overnight-sentiment' && (
          <div className="form-group full-width">
            <label htmlFor="bf-gap-thresh">Overnight Gap Threshold (%)</label>
            <input
              id="bf-gap-thresh"
              name="thresholdPercent"
              type="number"
              step="0.05"
              min="0"
              value={form.thresholdPercent ?? 0.2}
              onChange={handleChange}
            />
          </div>
        )}

        {form.strategy === 'consistent-momentum' && (
          <>
            <div className="form-group">
              <label htmlFor="bf-cm-lookback">Lookback Bars</label>
              <input
                id="bf-cm-lookback"
                name="lookbackPeriod"
                type="number"
                min="20"
                value={form.lookbackPeriod ?? 120}
                onChange={handleChange}
              />
            </div>
            <div className="form-group">
              <label htmlFor="bf-cm-buckets">Number of Buckets</label>
              <input
                id="bf-cm-buckets"
                name="numBuckets"
                type="number"
                min="2"
                value={form.numBuckets ?? 6}
                onChange={handleChange}
              />
            </div>
            <div className="form-group full-width">
              <label htmlFor="bf-cm-ratio">Min Positive Ratio (e.g. 0.67 for 67%)</label>
              <input
                id="bf-cm-ratio"
                name="minConsistencyRatio"
                type="number"
                step="0.05"
                min="0.1"
                max="1.0"
                value={form.minConsistencyRatio ?? 0.67}
                onChange={handleChange}
              />
            </div>
          </>
        )}

        {/* Slippage */}
        <div className="form-group">
          <label htmlFor="bf-slip">Slippage (%)</label>
          <input
            id="bf-slip"
            name="slippagePercent"
            type="number"
            min="0"
            step="0.01"
            value={form.slippagePercent}
            onChange={handleChange}
          />
        </div>

        {/* Transaction Fee */}
        <div className="form-group">
          <label htmlFor="bf-fee">Transaction Fee (%)</label>
          <input
            id="bf-fee"
            name="transactionFeePercent"
            type="number"
            min="0"
            step="0.01"
            value={form.transactionFeePercent}
            onChange={handleChange}
          />
        </div>

        <div className="form-divider" />

        {/* Start Date */}
        <div className="form-group">
          <label htmlFor="bf-start">Start Date</label>
          <input
            id="bf-start"
            name="startDate"
            type="date"
            value={form.startDate}
            onChange={handleChange}
          />
        </div>

        {/* End Date */}
        <div className="form-group">
          <label htmlFor="bf-end">End Date</label>
          <input
            id="bf-end"
            name="endDate"
            type="date"
            value={form.endDate}
            onChange={handleChange}
          />
        </div>

        {/* CSV File Upload */}
        <div
          className={`file-upload-area ${csvFile ? 'has-file' : ''}`}
          onClick={() => fileRef.current?.click()}
        >
          <div className="file-upload-icon">
            <Upload size={18} />
          </div>
          <div className="file-upload-text">
            <div className="file-upload-label">
              {csvFile ? csvFile.name : 'Upload CSV Data'}
            </div>
            <div className="file-upload-hint">
              {csvFile
                ? `${(csvFile.size / 1024).toFixed(1)} KB`
                : 'Optional — uses sample data if not provided'}
            </div>
          </div>
          {csvFile && (
            <button type="button" className="file-upload-remove" onClick={removeFile}>
              <X size={16} />
            </button>
          )}
          <input
            ref={fileRef}
            type="file"
            accept=".csv"
            className="file-upload-input"
            onChange={handleFileChange}
          />
        </div>

        {/* Error */}
        {error && (
          <div className="form-error">
            <AlertCircle size={16} />
            {error}
          </div>
        )}

        {/* Submit */}
        <div className="form-submit">
          <button type="submit" className="btn-primary" disabled={isLoading}>
            {isLoading ? (
              <>
                <span className="spinner" />
                Running Backtest...
              </>
            ) : (
              <>
                <Play size={16} />
                Run Backtest
              </>
            )}
          </button>
        </div>
      </div>
    </form>
  )
}
