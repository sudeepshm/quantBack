import { useState, useMemo } from 'react'
import { List, ArrowUp, ArrowDown } from 'lucide-react'
import './TradeLog.css'

function formatCurrency(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(value)
}

function formatDate(ts) {
  const d = new Date(ts)
  return d.toLocaleDateString('en-IN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

const COLUMNS = [
  { key: 'timestamp', label: 'Date' },
  { key: 'side', label: 'Side' },
  { key: 'orderType', label: 'Trigger' },
  { key: 'quantity', label: 'Qty' },
  { key: 'price', label: 'Price' },
  { key: 'fee', label: 'Fee' },
  { key: 'grossValue', label: 'Gross Value' },
]

export default function TradeLog({ trades }) {
  const [sortKey, setSortKey] = useState('timestamp')
  const [sortAsc, setSortAsc] = useState(true)

  const sorted = useMemo(() => {
    if (!trades || trades.length === 0) return []
    return [...trades].sort((a, b) => {
      let va = a[sortKey]
      let vb = b[sortKey]
      if (sortKey === 'timestamp') {
        va = new Date(va).getTime()
        vb = new Date(vb).getTime()
      }
      if (typeof va === 'string') {
        return sortAsc ? va.localeCompare(vb) : vb.localeCompare(va)
      }
      return sortAsc ? va - vb : vb - va
    })
  }, [trades, sortKey, sortAsc])

  const handleSort = (key) => {
    if (sortKey === key) {
      setSortAsc(!sortAsc)
    } else {
      setSortKey(key)
      setSortAsc(true)
    }
  }

  if (!trades || trades.length === 0) return null

  return (
    <div className="trade-log-container box" style={{ marginTop: 24 }}>
      <div className="card-title">
        <div className="trade-log-title-left">
          <List size={16} />
          Trade Log
        </div>
        <span className="trade-log-count">{trades.length} trades</span>
      </div>

      <div className="trade-log-wrapper">
        <table className="trade-log-table">
          <thead>
            <tr>
              {COLUMNS.map((col) => (
                <th
                  key={col.key}
                  className={sortKey === col.key ? 'sorted' : ''}
                  onClick={() => handleSort(col.key)}
                >
                  {col.label}
                  <span className="sort-icon">
                    {sortKey === col.key ? (sortAsc ? '▲' : '▼') : '⬍'}
                  </span>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {sorted.map((trade, i) => (
              <tr key={i}>
                <td>{formatDate(trade.timestamp)}</td>
                <td>
                  <span className={`side-badge ${trade.side.toLowerCase()}`}>
                    {trade.side === 'BUY' ? <ArrowUp size={12} /> : <ArrowDown size={12} />}
                    {trade.side}
                  </span>
                </td>
                <td>
                  <span className={`order-type-badge ${(trade.orderType || 'MARKET').toLowerCase()}`}>
                    {trade.orderType === 'STOP_LOSS'
                      ? 'Stop Loss'
                      : trade.orderType === 'TAKE_PROFIT'
                      ? 'Take Profit'
                      : trade.orderType === 'TRAILING_STOP'
                      ? 'Trailing Stop'
                      : 'Signal'}
                  </span>
                </td>
                <td>{trade.quantity}</td>
                <td>{formatCurrency(trade.price)}</td>
                <td>{formatCurrency(trade.fee)}</td>
                <td>{formatCurrency(trade.grossValue)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
