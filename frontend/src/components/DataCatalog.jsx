import { useState, useMemo } from 'react'
import {
  Database,
  Download,
  Play,
  CheckCircle2,
  AlertTriangle,
  FileText,
  TrendingUp,
  Table,
  Upload,
  Eye,
  EyeOff,
  Filter,
  Sparkles,
  Layers,
  Info
} from 'lucide-react'
import './DataCatalog.css'

// Built-in Curated OHLCV Datasets (Strictly formatted for CsvBarDataLoader)
export const CURATED_DATASETS = [
  {
    id: 'nifty-50-daily',
    symbol: 'NIFTY50',
    name: 'NIFTY 50 Index (Daily)',
    assetClass: 'Indices',
    bars: 22,
    dateRange: '2026-01-01 to 2026-01-30',
    timeframe: 'Daily (1D)',
    openPrice: 21700.0,
    closePrice: 22180.0,
    highPrice: 22200.0,
    lowPrice: 21680.0,
    totalReturn: '+2.21%',
    annualizedVol: '11.4%',
    description: 'Benchmark 50-stock blue-chip index of the National Stock Exchange of India. High liquidity, tight bid-ask spreads, ideal for moving average crossover and RSI models.',
    csvData: `timestamp,open,high,low,close,volume
2026-01-01T09:15:00,21700.00,21750.00,21680.00,21720.00,120000
2026-01-02T09:15:00,21725.00,21800.00,21710.00,21780.00,135000
2026-01-05T09:15:00,21790.00,21850.00,21750.00,21820.00,140000
2026-01-06T09:15:00,21830.00,21900.00,21800.00,21890.00,150000
2026-01-07T09:15:00,21900.00,21950.00,21850.00,21910.00,130000
2026-01-08T09:15:00,21920.00,22000.00,21900.00,21980.00,160000
2026-01-09T09:15:00,22000.00,22050.00,21950.00,22020.00,145000
2026-01-12T09:15:00,22030.00,22100.00,22010.00,22080.00,155000
2026-01-13T09:15:00,22090.00,22120.00,22040.00,22060.00,125000
2026-01-14T09:15:00,22050.00,22080.00,21980.00,22000.00,130000
2026-01-15T09:15:00,21990.00,22020.00,21920.00,21940.00,140000
2026-01-16T09:15:00,21930.00,21960.00,21850.00,21880.00,135000
2026-01-19T09:15:00,21870.00,21900.00,21810.00,21830.00,128000
2026-01-20T09:15:00,21820.00,21850.00,21750.00,21790.00,132000
2026-01-21T09:15:00,21780.00,21820.00,21720.00,21750.00,120000
2026-01-22T09:15:00,21760.00,21810.00,21740.00,21790.00,122000
2026-01-23T09:15:00,21800.00,21860.00,21780.00,21850.00,129000
2026-01-26T09:15:00,21860.00,21920.00,21840.00,21910.00,138000
2026-01-27T09:15:00,21920.00,22000.00,21900.00,21990.00,145000
2026-01-28T09:15:00,22000.00,22080.00,21980.00,22060.00,152000
2026-01-29T09:15:00,22070.00,22150.00,22050.00,22130.00,160000
2026-01-30T09:15:00,22140.00,22200.00,22100.00,22180.00,158000`,
  },
  {
    id: 'bank-nifty-daily',
    symbol: 'BANKNIFTY',
    name: 'BANK NIFTY Sectoral Index (Daily)',
    assetClass: 'Indices',
    bars: 22,
    dateRange: '2026-01-01 to 2026-01-30',
    timeframe: 'Daily (1D)',
    openPrice: 47850.0,
    closePrice: 49420.0,
    highPrice: 49500.0,
    lowPrice: 47600.0,
    totalReturn: '+3.28%',
    annualizedVol: '17.2%',
    description: 'High-beta Indian banking sector index representing the 12 most liquid banking enterprises. Excellent for Donchian Channel breakout and trend-following regimes.',
    csvData: `timestamp,open,high,low,close,volume
2026-01-01T09:15:00,47850.00,48000.00,47720.00,47980.00,210000
2026-01-02T09:15:00,48010.00,48200.00,47950.00,48150.00,225000
2026-01-05T09:15:00,48180.00,48350.00,48050.00,48290.00,240000
2026-01-06T09:15:00,48320.00,48500.00,48200.00,48460.00,260000
2026-01-07T09:15:00,48480.00,48650.00,48350.00,48580.00,230000
2026-01-08T09:15:00,48600.00,48800.00,48500.00,48720.00,280000
2026-01-09T09:15:00,48750.00,48950.00,48650.00,48890.00,255000
2026-01-12T09:15:00,48910.00,49100.00,48800.00,49020.00,270000
2026-01-13T09:15:00,49050.00,49150.00,48850.00,48900.00,215000
2026-01-14T09:15:00,48880.00,48950.00,48600.00,48680.00,220000
2026-01-15T09:15:00,48650.00,48750.00,48400.00,48490.00,245000
2026-01-16T09:15:00,48450.00,48580.00,48200.00,48300.00,235000
2026-01-19T09:15:00,48280.00,48400.00,48100.00,48180.00,218000
2026-01-20T09:15:00,48150.00,48250.00,47900.00,48020.00,225000
2026-01-21T09:15:00,48000.00,48150.00,47800.00,47920.00,210000
2026-01-22T09:15:00,47950.00,48200.00,47850.00,48100.00,215000
2026-01-23T09:15:00,48120.00,48380.00,48050.00,48320.00,230000
2026-01-26T09:15:00,48350.00,48600.00,48280.00,48550.00,250000
2026-01-27T09:15:00,48580.00,48900.00,48500.00,48820.00,265000
2026-01-28T09:15:00,48850.00,49150.00,48780.00,49080.00,285000
2026-01-29T09:15:00,49100.00,49350.00,49000.00,49280.00,300000
2026-01-30T09:15:00,49300.00,49500.00,49200.00,49420.00,295000`,
  },
  {
    id: 'spy-us-daily',
    symbol: 'SPY',
    name: 'S&P 500 ETF Trust (SPY - Daily)',
    assetClass: 'Equities',
    bars: 22,
    dateRange: '2026-01-01 to 2026-01-30',
    timeframe: 'Daily (1D)',
    openPrice: 5020.0,
    closePrice: 5190.0,
    highPrice: 5210.0,
    lowPrice: 5010.0,
    totalReturn: '+3.39%',
    annualizedVol: '12.6%',
    description: 'The premier US large-cap benchmark tracking 500 leading companies. Widely used in academic finance literature for testing trend-following and momentum alphas.',
    csvData: `timestamp,open,high,low,close,volume
2026-01-01T09:30:00,5020.00,5045.00,5010.00,5038.00,4500000
2026-01-02T09:30:00,5040.00,5060.00,5030.00,5055.00,4800000
2026-01-05T09:30:00,5060.00,5085.00,5050.00,5078.00,5100000
2026-01-06T09:30:00,5080.00,5110.00,5075.00,5102.00,5300000
2026-01-07T09:30:00,5105.00,5125.00,5090.00,5118.00,4700000
2026-01-08T09:30:00,5120.00,5140.00,5110.00,5135.00,5500000
2026-01-09T09:30:00,5140.00,5165.00,5130.00,5158.00,5200000
2026-01-12T09:30:00,5160.00,5180.00,5150.00,5172.00,5600000
2026-01-13T09:30:00,5175.00,5185.00,5155.00,5160.00,4400000
2026-01-14T09:30:00,5158.00,5168.00,5130.00,5140.00,4600000
2026-01-15T09:30:00,5138.00,5150.00,5115.00,5125.00,4900000
2026-01-16T09:30:00,5120.00,5135.00,5098.00,5110.00,4800000
2026-01-19T09:30:00,5108.00,5120.00,5088.00,5095.00,4500000
2026-01-20T09:30:00,5092.00,5105.00,5070.00,5082.00,4700000
2026-01-21T09:30:00,5080.00,5095.00,5065.00,5075.00,4400000
2026-01-22T09:30:00,5078.00,5100.00,5070.00,5092.00,4500000
2026-01-23T09:30:00,5095.00,5120.00,5088.00,5115.00,4900000
2026-01-26T09:30:00,5118.00,5145.00,5110.00,5138.00,5200000
2026-01-27T09:30:00,5140.00,5170.00,5132.00,5162.00,5400000
2026-01-28T09:30:00,5165.00,5190.00,5155.00,5180.00,5700000
2026-01-29T09:30:00,5182.00,5205.00,5172.00,5195.00,5900000
2026-01-30T09:30:00,5198.00,5215.00,5185.00,5190.00,5800000`,
  },
  {
    id: 'btc-usd-daily',
    symbol: 'BTC-USD',
    name: 'Bitcoin / US Dollar (Daily)',
    assetClass: 'Crypto',
    bars: 22,
    dateRange: '2026-01-01 to 2026-01-30',
    timeframe: 'Daily (1D)',
    openPrice: 61400.0,
    closePrice: 71200.0,
    highPrice: 71800.0,
    lowPrice: 60800.0,
    totalReturn: '+15.96%',
    annualizedVol: '48.5%',
    description: 'Premier digital currency exhibiting pronounced non-linear volatility clustering and explosive trend persistence. Well suited for Donchian Channel (#28) & MACD (#73).',
    csvData: `timestamp,open,high,low,close,volume
2026-01-01T00:00:00,61400.00,62200.00,60800.00,61950.00,18500
2026-01-02T00:00:00,61980.00,63100.00,61700.00,62800.00,21000
2026-01-05T00:00:00,62850.00,64200.00,62500.00,63900.00,24500
2026-01-06T00:00:00,63950.00,65400.00,63600.00,65100.00,28000
2026-01-07T00:00:00,65120.00,66200.00,64700.00,65950.00,23000
2026-01-08T00:00:00,66000.00,67500.00,65800.00,67200.00,31000
2026-01-09T00:00:00,67250.00,68900.00,66900.00,68450.00,29500
2026-01-12T00:00:00,68500.00,69800.00,68100.00,69200.00,32000
2026-01-13T00:00:00,69250.00,69600.00,67800.00,68100.00,25000
2026-01-14T00:00:00,68050.00,68400.00,66500.00,66900.00,26500
2026-01-15T00:00:00,66850.00,67400.00,65200.00,65800.00,28000
2026-01-16T00:00:00,65750.00,66300.00,64400.00,64900.00,27000
2026-01-19T00:00:00,64850.00,65500.00,64100.00,64500.00,23500
2026-01-20T00:00:00,64450.00,65100.00,63700.00,64200.00,24000
2026-01-21T00:00:00,64180.00,64900.00,63800.00,64400.00,22500
2026-01-22T00:00:00,64420.00,65600.00,64200.00,65300.00,25500
2026-01-23T00:00:00,65350.00,66800.00,65100.00,66500.00,27000
2026-01-26T00:00:00,66550.00,68200.00,66300.00,67900.00,30500
2026-01-27T00:00:00,67950.00,69500.00,67700.00,69100.00,33000
2026-01-28T00:00:00,69150.00,70600.00,68900.00,70200.00,36000
2026-01-29T00:00:00,70250.00,71400.00,70000.00,70900.00,38500
2026-01-30T00:00:00,70950.00,71800.00,70600.00,71200.00,37000`,
  },
  {
    id: 'gold-spot-daily',
    symbol: 'XAUUSD',
    name: 'Gold Spot / US Dollar (Daily)',
    assetClass: 'Commodities',
    bars: 22,
    dateRange: '2026-01-01 to 2026-01-30',
    timeframe: 'Daily (1D)',
    openPrice: 2310.0,
    closePrice: 2425.0,
    highPrice: 2435.0,
    lowPrice: 2305.0,
    totalReturn: '+4.98%',
    annualizedVol: '9.8%',
    description: 'Gold spot bullion. Essential non-correlated macro hedge for dual-momentum cross-asset testing and capital preservation during equities drawdowns.',
    csvData: `timestamp,open,high,low,close,volume
2026-01-01T00:00:00,2310.00,2320.00,2305.00,2318.00,45000
2026-01-02T00:00:00,2319.00,2330.00,2315.00,2326.00,48000
2026-01-05T00:00:00,2327.00,2342.00,2322.00,2338.00,52000
2026-01-06T00:00:00,2339.00,2355.00,2335.00,2350.00,56000
2026-01-07T00:00:00,2351.00,2362.00,2345.00,2358.00,49000
2026-01-08T00:00:00,2359.00,2372.00,2354.00,2368.00,58000
2026-01-09T00:00:00,2369.00,2385.00,2365.00,2380.00,54000
2026-01-12T00:00:00,2381.00,2395.00,2378.00,2390.00,60000
2026-01-13T00:00:00,2391.00,2398.00,2380.00,2385.00,47000
2026-01-14T00:00:00,2384.00,2390.00,2372.00,2378.00,48000
2026-01-15T00:00:00,2377.00,2385.00,2365.00,2372.00,51000
2026-01-16T00:00:00,2371.00,2380.00,2360.00,2366.00,49000
2026-01-19T00:00:00,2365.00,2375.00,2358.00,2362.00,46000
2026-01-20T00:00:00,2361.00,2370.00,2352.00,2359.00,47000
2026-01-21T00:00:00,2358.00,2368.00,2354.00,2364.00,45000
2026-01-22T00:00:00,2365.00,2378.00,2360.00,2374.00,48000
2026-01-23T00:00:00,2375.00,2390.00,2370.00,2385.00,52000
2026-01-26T00:00:00,2386.00,2402.00,2382.00,2398.00,56000
2026-01-27T00:00:00,2399.00,2415.00,2395.00,2410.00,59000
2026-01-28T00:00:00,2411.00,2425.00,2408.00,2420.00,62000
2026-01-29T00:00:00,2421.00,2432.00,2418.00,2428.00,64000
2026-01-30T00:00:00,2429.00,2435.00,2422.00,2425.00,61000`,
  },
]

export default function DataCatalog({ onLoadDataset }) {
  const [selectedCategory, setSelectedCategory] = useState('ALL')
  const [previewDatasetId, setPreviewDatasetId] = useState(null)
  const [validationResult, setValidationResult] = useState(null)
  const [uploadedFile, setUploadedFile] = useState(null)
  const [isValidating, setIsValidating] = useState(false)

  const categories = ['ALL', 'Indices', 'Equities', 'Crypto', 'Commodities']

  const filteredDatasets = useMemo(() => {
    if (selectedCategory === 'ALL') return CURATED_DATASETS
    return CURATED_DATASETS.filter((d) => d.assetClass === selectedCategory)
  }, [selectedCategory])

  const handleDownloadCsv = (dataset) => {
    const blob = new Blob([dataset.csvData.trim()], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `${dataset.symbol.toLowerCase()}_sample_data.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }

  const handleLoad = (dataset) => {
    if (onLoadDataset) {
      const blob = new Blob([dataset.csvData.trim()], { type: 'text/csv' })
      const file = new File([blob], `${dataset.symbol.toLowerCase()}_sample.csv`, {
        type: 'text/csv',
      })
      onLoadDataset(file, dataset.symbol)
    }
  }

  // Parse and validate custom user CSV
  const handleValidateFile = (e) => {
    const file = e.target.files?.[0]
    if (!file) return

    setUploadedFile(file)
    setIsValidating(true)
    setValidationResult(null)

    const reader = new FileReader()
    reader.onload = (event) => {
      try {
        const text = event.target?.result
        if (typeof text !== 'string') throw new Error('File could not be read as text')

        const lines = text
          .split(/\r?\n/)
          .map((l) => l.trim())
          .filter((l) => l.length > 0)

        if (lines.length < 2) {
          throw new Error('CSV must contain a header row and at least one data row.')
        }

        const header = lines[0].toLowerCase().split(',').map((h) => h.trim())
        const requiredCols = ['timestamp', 'open', 'high', 'low', 'close', 'volume']
        const missingCols = requiredCols.filter((col) => !header.includes(col))

        if (missingCols.length > 0) {
          throw new Error(`Missing required columns: ${missingCols.join(', ')}. Found: ${header.join(', ')}`)
        }

        const colIndices = {
          timestamp: header.indexOf('timestamp'),
          open: header.indexOf('open'),
          high: header.indexOf('high'),
          low: header.indexOf('low'),
          close: header.indexOf('close'),
          volume: header.indexOf('volume'),
        }

        const rows = []
        let lastDate = null
        let chronologicalError = null

        for (let i = 1; i < lines.length; i++) {
          const parts = lines[i].split(',').map((p) => p.trim())
          if (parts.length < header.length) continue

          const ts = parts[colIndices.timestamp]
          const open = parseFloat(parts[colIndices.open])
          const high = parseFloat(parts[colIndices.high])
          const low = parseFloat(parts[colIndices.low])
          const close = parseFloat(parts[colIndices.close])
          const vol = parseFloat(parts[colIndices.volume])

          if (isNaN(open) || isNaN(high) || isNaN(low) || isNaN(close) || isNaN(vol)) {
            throw new Error(`Invalid numeric value found on row ${i + 1}: "${lines[i]}"`)
          }

          if (high < low) {
            throw new Error(`High price (${high}) is less than Low price (${low}) on row ${i + 1}`)
          }

          const parsedDate = new Date(ts)
          if (isNaN(parsedDate.getTime())) {
            throw new Error(`Unparseable ISO/datetime timestamp "${ts}" on row ${i + 1}`)
          }

          if (lastDate && parsedDate.getTime() < lastDate.getTime() && !chronologicalError) {
            chronologicalError = `Timestamp inversion detected on row ${i + 1}: ${ts} occurred before ${lastDate.toISOString()}. Chronological order is mandatory to prevent look-ahead bias!`
          }
          lastDate = parsedDate

          rows.push({
            timestamp: ts,
            open,
            high,
            low,
            close,
            volume: vol,
          })
        }

        if (chronologicalError) {
          throw new Error(chronologicalError)
        }

        const startPrice = rows[0].open
        const endPrice = rows[rows.length - 1].close
        const totalReturnPct = (((endPrice - startPrice) / startPrice) * 100).toFixed(2)

        setValidationResult({
          valid: true,
          totalRows: rows.length,
          startDate: rows[0].timestamp,
          endDate: rows[rows.length - 1].timestamp,
          startPrice,
          endPrice,
          totalReturnPct: `${totalReturnPct > 0 ? '+' : ''}${totalReturnPct}%`,
          sampleRows: rows.slice(0, 5),
          rawText: text,
        })
      } catch (err) {
        setValidationResult({
          valid: false,
          error: err.message,
        })
      } finally {
        setIsValidating(false)
      }
    }
    reader.readAsText(file)
  }

  const handleLoadValidatedCustomFile = () => {
    if (uploadedFile && validationResult?.valid && onLoadDataset) {
      const derivedSymbol = uploadedFile.name.replace(/\.[^/.]+$/, '').toUpperCase()
      onLoadDataset(uploadedFile, derivedSymbol)
    }
  }

  return (
    <div className="data-catalog-container">
      {/* Header Banner */}
      <div className="data-header">
        <div className="data-header-badge">
          <Database size={14} />
          <span>Historical Market Data Repository</span>
        </div>
        <h2 className="d">curated datasets & csv inspector.</h2>
        <p className="lead">
          Inspect benchmark index and multi-asset price series, preview historical candle bars, download clean CSV data, or validate your own custom market feeds with zero look-ahead bias.
        </p>

        {/* Category Filters */}
        <div className="data-category-filters">
          <div className="filter-label">
            <Filter size={14} />
            <span>Filter Asset Class:</span>
          </div>
          <div className="filter-pills">
            {categories.map((cat) => (
              <button
                key={cat}
                type="button"
                className={`filter-pill ${selectedCategory === cat ? 'active' : ''}`}
                onClick={() => setSelectedCategory(cat)}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Curated Datasets Grid */}
      <div className="datasets-grid">
        {filteredDatasets.map((dataset) => {
          const isPreviewing = previewDatasetId === dataset.id
          const rows = dataset.csvData
            .trim()
            .split('\n')
            .slice(1, 6)
            .map((line) => line.split(','))

          return (
            <div key={dataset.id} className="dataset-card">
              <div className="dataset-card-top">
                <div className="dataset-symbol-badge">
                  <span className="dataset-symbol">{dataset.symbol}</span>
                  <span className="dataset-class-tag">{dataset.assetClass}</span>
                </div>
                <div className="dataset-timeframe">{dataset.timeframe}</div>
              </div>

              <h3 className="dataset-name">{dataset.name}</h3>
              <p className="dataset-desc">{dataset.description}</p>

              {/* Statistics Row */}
              <div className="dataset-stats-grid">
                <div className="dataset-stat">
                  <div className="stat-label">Bar Count</div>
                  <div className="stat-value">{dataset.bars} Bars</div>
                </div>
                <div className="dataset-stat">
                  <div className="stat-label">Range Return</div>
                  <div className="stat-value pos">{dataset.totalReturn}</div>
                </div>
                <div className="dataset-stat">
                  <div className="stat-label">Annualized Vol</div>
                  <div className="stat-value">{dataset.annualizedVol}</div>
                </div>
                <div className="dataset-stat">
                  <div className="stat-label">Latest Close</div>
                  <div className="stat-value">{dataset.closePrice.toLocaleString()}</div>
                </div>
              </div>

              <div className="dataset-date-range">
                <FileText size={13} />
                <span>{dataset.dateRange}</span>
              </div>

              {/* Action Buttons */}
              <div className="dataset-actions">
                <button
                  type="button"
                  className="dataset-btn primary"
                  onClick={() => handleLoad(dataset)}
                >
                  <Play size={14} fill="#16081F" />
                  <span>Load in Backtester</span>
                </button>

                <button
                  type="button"
                  className="dataset-btn secondary"
                  onClick={() => handleDownloadCsv(dataset)}
                  title="Download raw CSV file"
                >
                  <Download size={14} />
                  <span>CSV</span>
                </button>

                <button
                  type="button"
                  className={`dataset-btn secondary ${isPreviewing ? 'active' : ''}`}
                  onClick={() => setPreviewDatasetId(isPreviewing ? null : dataset.id)}
                  title="Toggle candle preview"
                >
                  {isPreviewing ? <EyeOff size={14} /> : <Eye size={14} />}
                  <span>{isPreviewing ? 'Hide' : 'Preview'}</span>
                </button>
              </div>

              {/* Collapsible Preview Table */}
              {isPreviewing && (
                <div className="dataset-preview-drawer">
                  <div className="preview-drawer-header">
                    <Table size={13} />
                    <span>Sample OHLCV Bars (First 5 Rows)</span>
                  </div>
                  <div className="preview-table-wrap">
                    <table className="preview-table">
                      <thead>
                        <tr>
                          <th>Timestamp</th>
                          <th>Open</th>
                          <th>High</th>
                          <th>Low</th>
                          <th>Close</th>
                          <th>Volume</th>
                        </tr>
                      </thead>
                      <tbody>
                        {rows.map((r, i) => (
                          <tr key={i}>
                            <td className="mono">{r[0]}</td>
                            <td>{r[1]}</td>
                            <td>{r[2]}</td>
                            <td>{r[3]}</td>
                            <td className="bold">{r[4]}</td>
                            <td>{Number(r[5]).toLocaleString()}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </div>
          )
        })}
      </div>

      {/* CSV Inspector & Custom Data Validator Section */}
      <div className="csv-validator-section">
        <div className="validator-header">
          <div className="validator-badge">
            <Sparkles size={14} />
            <span>Interactive Data Diagnostics</span>
          </div>
          <h3 className="validator-title">custom csv data inspector & validator</h3>
          <p className="validator-sub">
            QuantBack verifies that your time-series strictly conforms to chronological ordering without future price leakage. Drop any custom CSV to validate column headers, numeric constraints, and date integrity before running simulations.
          </p>
        </div>

        <div className="validator-body">
          <div className="validator-dropzone">
            <Upload size={28} />
            <div className="dropzone-text">
              <span className="dropzone-main">Choose or drop your CSV file here</span>
              <span className="dropzone-sub">
                Supported schema: timestamp, open, high, low, close, volume
              </span>
            </div>
            <label className="dropzone-file-btn">
              Browse Files
              <input
                type="file"
                accept=".csv"
                className="hidden-file-input"
                onChange={handleValidateFile}
              />
            </label>
          </div>

          {isValidating && (
            <div className="validation-loading">
              <div className="spinner" />
              <span>Analyzing historical candle integrity...</span>
            </div>
          )}

          {validationResult && (
            <div className={`validation-result-card ${validationResult.valid ? 'success' : 'error'}`}>
              <div className="result-header">
                {validationResult.valid ? (
                  <>
                    <CheckCircle2 size={20} className="result-icon-success" />
                    <div>
                      <h4 className="result-title">Dataset Validated Successfully</h4>
                      <p className="result-desc">
                        {uploadedFile?.name} passes all chronological, schema, and quantitative checks.
                      </p>
                    </div>
                  </>
                ) : (
                  <>
                    <AlertTriangle size={20} className="result-icon-error" />
                    <div>
                      <h4 className="result-title">Integrity Check Failed</h4>
                      <p className="result-desc">{validationResult.error}</p>
                    </div>
                  </>
                )}
              </div>

              {validationResult.valid && (
                <>
                  <div className="result-stats-row">
                    <div className="r-stat">
                      <span className="r-label">Total Valid Bars</span>
                      <span className="r-val">{validationResult.totalRows}</span>
                    </div>
                    <div className="r-stat">
                      <span className="r-label">Start Date</span>
                      <span className="r-val mono">{validationResult.startDate}</span>
                    </div>
                    <div className="r-stat">
                      <span className="r-label">End Date</span>
                      <span className="r-val mono">{validationResult.endDate}</span>
                    </div>
                    <div className="r-stat">
                      <span className="r-label">Period Return</span>
                      <span className="r-val pos">{validationResult.totalReturnPct}</span>
                    </div>
                  </div>

                  <div className="validation-actions">
                    <button
                      type="button"
                      className="dataset-btn primary"
                      onClick={handleLoadValidatedCustomFile}
                    >
                      <Play size={14} fill="#16081F" />
                      <span>Load Validated Data into Backtester</span>
                    </button>
                  </div>
                </>
              )}
            </div>
          )}
        </div>

        {/* Data Architecture Rules Note */}
        <div className="data-specs-note">
          <Info size={18} />
          <div>
            <strong>Zero Look-Ahead Standard:</strong> All backtest simulations process price bars sequentially. To avoid look-ahead bias, timestamps must be monotonically ascending. Signal evaluation is calculated at bar close $T$, with market execution happening at bar open $T+1$ or bar close $T$ with transaction slippage.
          </div>
        </div>
      </div>
    </div>
  )
}
