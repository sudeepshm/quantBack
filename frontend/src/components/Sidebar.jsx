import { BarChart3, LineChart, Settings, Database, Activity } from 'lucide-react'
import './Sidebar.css'

const navItems = [
  { id: 'backtests', label: 'Backtests', icon: BarChart3 },
  { id: 'strategies', label: 'Strategies', icon: LineChart },
  { id: 'data', label: 'Data', icon: Database },
]

export default function Sidebar({ apiStatus }) {
  return (
    <aside className="sidebar">
      {/* Brand */}
      <div className="sidebar-brand">
        <div className="sidebar-logo">
          <Activity size={20} strokeWidth={2.5} />
        </div>
        <div className="sidebar-brand-text">
          <span className="sidebar-brand-name">QuantBack</span>
          <span className="sidebar-brand-tag">Backtesting Engine</span>
        </div>
      </div>

      {/* Navigation */}
      <nav className="sidebar-nav">
        {navItems.map((item) => (
          <button
            key={item.id}
            className={`sidebar-nav-item ${item.id === 'backtests' ? 'active' : ''}`}
          >
            <item.icon size={20} />
            <span>{item.label}</span>
          </button>
        ))}
      </nav>

      {/* Footer */}
      <div className="sidebar-footer">
        <div className="sidebar-status">
          <span className={`sidebar-status-dot ${apiStatus === 'connected' ? '' : 'offline'}`} />
          <span>
            {apiStatus === 'connected' ? 'API Connected' : apiStatus === 'checking' ? 'Connecting...' : 'API Offline'}
          </span>
        </div>
        <div className="sidebar-version">v1.0.0</div>
      </div>
    </aside>
  )
}
