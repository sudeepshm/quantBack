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
  'halloween-effect': { entryMonth: 11, exitMonth: 5 },
  'bollinger-bands': { period: 20, stdDevMultiplier: 2.0 },
  'day-of-the-week': { entryDayOfWeek: 2, exitDayOfWeek: 5 },
}

const DEFAULT_VALUES = {
  symbol: 'NIFTY',
  strategy: 'moving-average',
  initialCapital: 100000,
  orderQuantity: 10,
  slippagePercent: 0.05,
  transactionFeePercent: 0.1,
  stopLossPercent: 0,
  takeProfitPercent: 0,
  trailingStopPercent: 0,
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

    if (form.strategy === 'halloween-effect' && Number(form.entryMonth) === Number(form.exitMonth)) {
      setError('Entry month and exit month cannot be identical')
      return
    }

    if (form.strategy === 'bollinger-bands' && form.period < 2) {
      setError('Bollinger Bands period must be at least 2')
      return
    }

    if (form.strategy === 'day-of-the-week' && Number(form.entryDayOfWeek) > Number(form.exitDayOfWeek)) {
      setError('Entry day cannot be after exit day')
      return
    }

    try {
      await onSubmit(form, csvFile)
    } catch (err) {
      setError(err.message || 'Failed to run backtest')
    }
  }

  return (
    <form className="backtest-form box" onSubmit={handleSubmit}>
      <div className="card-title">
        <Settings2 size={18} />
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
                <option value="overnight-sentiment">Overnight Sentiment Anomaly (Quantpedia #34 & #48)</option>
                <option value="consistent-momentum">Consistent Momentum Strategy (Quantpedia #11)</option>
                <option value="january-barometer">January Barometer Effect (Quantpedia #30 & #31)</option>
                <option value="halloween-effect">The Halloween Effect / Sell in May (Quantpedia #49)</option>
                <option value="bollinger-bands">Bollinger Bands Mean Reversion (Quantpedia #22 & #63)</option>
                <option value="day-of-the-week">Day-of-the-Week / Weekend Effect (Quantpedia #16)</option>
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

        {form.strategy === 'halloween-effect' && (
          <>
            <div className="form-group">
              <label htmlFor="bf-entry-month">Entry Month (Buy)</label>
              <select
                id="bf-entry-month"
                name="entryMonth"
                value={form.entryMonth ?? 11}
                onChange={handleChange}
              >
                <option value={1}>1 - January</option>
                <option value={2}>2 - February</option>
                <option value={3}>3 - March</option>
                <option value={4}>4 - April</option>
                <option value={5}>5 - May</option>
                <option value={6}>6 - June</option>
                <option value={7}>7 - July</option>
                <option value={8}>8 - August</option>
                <option value={9}>9 - September</option>
                <option value={10}>10 - October</option>
                <option value={11}>11 - November</option>
                <option value={12}>12 - December</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="bf-exit-month">Exit Month (Sell / Cash)</label>
              <select
                id="bf-exit-month"
                name="exitMonth"
                value={form.exitMonth ?? 5}
                onChange={handleChange}
              >
                <option value={1}>1 - January</option>
                <option value={2}>2 - February</option>
                <option value={3}>3 - March</option>
                <option value={4}>4 - April</option>
                <option value={5}>5 - May</option>
                <option value={6}>6 - June</option>
                <option value={7}>7 - July</option>
                <option value={8}>8 - August</option>
                <option value={9}>9 - September</option>
                <option value={10}>10 - October</option>
                <option value={11}>11 - November</option>
                <option value={12}>12 - December</option>
              </select>
            </div>
          </>
        )}

        {form.strategy === 'bollinger-bands' && (
          <>
            <div className="form-group">
              <label htmlFor="bf-bb-period">Bands SMA Period</label>
              <input
                id="bf-bb-period"
                name="period"
                type="number"
                min="2"
                value={form.period ?? 20}
                onChange={handleChange}
              />
            </div>
            <div className="form-group">
              <label htmlFor="bf-bb-std">Std Dev Multiplier (k)</label>
              <input
                id="bf-bb-std"
                name="stdDevMultiplier"
                type="number"
                step="0.1"
                min="0.1"
                value={form.stdDevMultiplier ?? 2.0}
                onChange={handleChange}
              />
            </div>
          </>
        )}

        {form.strategy === 'day-of-the-week' && (
          <>
            <div className="form-group">
              <label htmlFor="bf-entry-day">Entry Day of Week</label>
              <select
                id="bf-entry-day"
                name="entryDayOfWeek"
                value={form.entryDayOfWeek ?? 2}
                onChange={handleChange}
              >
                <option value={1}>Monday</option>
                <option value={2}>Tuesday</option>
                <option value={3}>Wednesday</option>
                <option value={4}>Thursday</option>
                <option value={5}>Friday</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="bf-exit-day">Exit Day of Week</label>
              <select
                id="bf-exit-day"
                name="exitDayOfWeek"
                value={form.exitDayOfWeek ?? 5}
                onChange={handleChange}
              >
                <option value={1}>Monday</option>
                <option value={2}>Tuesday</option>
                <option value={3}>Wednesday</option>
                <option value={4}>Thursday</option>
                <option value={5}>Friday</option>
              </select>
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

        {/* Risk Controls Header */}
        <div className="form-divider" />

        {/* Stop Loss */}
        <div className="form-group">
          <label htmlFor="bf-sl">Stop Loss (%)</label>
          <input
            id="bf-sl"
            name="stopLossPercent"
            type="number"
            min="0"
            step="0.5"
            placeholder="0 = disabled"
            value={form.stopLossPercent === 0 ? '' : form.stopLossPercent}
            onChange={handleChange}
          />
        </div>

        {/* Take Profit */}
        <div className="form-group">
          <label htmlFor="bf-tp">Take Profit (%)</label>
          <input
            id="bf-tp"
            name="takeProfitPercent"
            type="number"
            min="0"
            step="0.5"
            placeholder="0 = disabled"
            value={form.takeProfitPercent === 0 ? '' : form.takeProfitPercent}
            onChange={handleChange}
          />
        </div>

        {/* Trailing Stop */}
        <div className="form-group full-width">
          <label htmlFor="bf-trail">Trailing Stop (%)</label>
          <input
            id="bf-trail"
            name="trailingStopPercent"
            type="number"
            min="0"
            step="0.5"
            placeholder="0 = disabled"
            value={form.trailingStopPercent === 0 ? '' : form.trailingStopPercent}
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
          <button type="submit" className="btn" style={{ width: '100%' }} disabled={isLoading}>
            {isLoading ? (
              <>
                <span className="spinner" />
                Simulating Historical Trades...
              </>
            ) : (
              <>
                <Play size={18} fill="#16081F" />
                Run Backtest Engine
              </>
            )}
          </button>
        </div>
      </div>
    </form>
  )
}
