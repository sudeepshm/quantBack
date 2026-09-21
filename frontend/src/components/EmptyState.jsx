import { BarChart3, ArrowRight } from 'lucide-react'

export default function EmptyState() {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '4rem 2rem',
        textAlign: 'center',
        animation: 'fadeIn 0.5s ease forwards',
      }}
    >
      {/* Animated icon */}
      <div
        style={{
          width: 80,
          height: 80,
          borderRadius: 'var(--radius-xl)',
          background: 'var(--accent-muted)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: '1.5rem',
          position: 'relative',
        }}
      >
        <BarChart3 size={36} color="var(--accent)" strokeWidth={1.5} />
        <div
          style={{
            position: 'absolute',
            inset: -4,
            border: '2px solid var(--accent-muted)',
            borderRadius: 'calc(var(--radius-xl) + 4px)',
            animation: 'pulse 2s ease-in-out infinite',
          }}
        />
      </div>

      <h2
        style={{
          fontSize: '1.25rem',
          fontWeight: 600,
          color: 'var(--text-primary)',
          marginBottom: '0.5rem',
        }}
      >
        No Backtest Results Yet
      </h2>

      <p
        style={{
          fontSize: '0.875rem',
          color: 'var(--text-secondary)',
          maxWidth: 400,
          lineHeight: 1.6,
          marginBottom: '1.5rem',
        }}
      >
        Configure your strategy parameters, upload your market data, and run a backtest to see
        performance metrics, equity curves, and trade logs.
      </p>

      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
          fontSize: '0.8125rem',
          color: 'var(--accent)',
          fontWeight: 500,
        }}
      >
        Configure & Run
        <ArrowRight size={16} />
      </div>
    </div>
  )
}
