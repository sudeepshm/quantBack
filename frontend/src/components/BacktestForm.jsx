import { useState, useRef } from 'react'
import { Play, Upload, X, AlertCircle, Settings2 } from 'lucide-react'
import './BacktestForm.css'

const DEFAULT_VALUES = {
  symbol: 'NIFTY',
  strategy: 'moving-average',
  initialCapital: 100000,
  fastPeriod: 20,
  slowPeriod: 50,
  orderQuantity: 10,
  slippagePercent: 0.05,
  transactionFeePercent: 0.1,
  startDate: '',
  endDate: '',
}

export default function BacktestForm({ onSubmit, isLoading, strategies }) {
  const [form, setForm] = useState(DEFAULT_VALUES)
  const [csvFile, setCsvFile] = useState(null)
  const [error, setError] = useState(null)
  const fileRef = useRef(null)

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

    if (form.fastPeriod >= form.slowPeriod) {
      setError('Fast MA period must be less than Slow MA period')
      return
    }
    if (form.initialCapital <= 0) {
      setError('Initial capital must be greater than 0')
      return
    }

    try {
      await onSubmit(form, csvFile)
    } catch (err) {
      setError(err.message || 'Failed to run backtest')
    }
  }

  // Find strategy params from the strategies list
  const activeStrategy = strategies?.find((s) => s.id === form.strategy)

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

        {/* Strategy */}
        <div className="form-group">
          <label htmlFor="bf-strategy">Strategy</label>
          <select id="bf-strategy" name="strategy" value={form.strategy} onChange={handleChange}>
            {strategies && strategies.length > 0 ? (
              strategies.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))
            ) : (
              <option value="moving-average">Moving Average Crossover</option>
            )}
          </select>
        </div>

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

        {/* Fast MA Period */}
        <div className="form-group">
          <label htmlFor="bf-fast">Fast MA Period</label>
          <input
            id="bf-fast"
            name="fastPeriod"
            type="number"
            min="1"
            value={form.fastPeriod}
            onChange={handleChange}
          />
        </div>

        {/* Slow MA Period */}
        <div className="form-group">
          <label htmlFor="bf-slow">Slow MA Period</label>
          <input
            id="bf-slow"
            name="slowPeriod"
            type="number"
            min="2"
            value={form.slowPeriod}
            onChange={handleChange}
          />
        </div>

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
