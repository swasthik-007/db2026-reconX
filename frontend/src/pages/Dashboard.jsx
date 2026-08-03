import React, { useMemo } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { useTradeStream } from '@hooks/useTradeStream.js';
function StatCard({ label, value }) { return <article className="stat-card"><h3>{label}</h3><p>{value}</p></article>; }
function Dashboard() {
  const { trades, isConnected } = useTradeStream();
  const summary = useMemo(() => trades.reduce((totals, trade) => { totals.portfolioValue += Number(trade.quantity ?? trade.qty ?? 0) * Number(trade.price ?? 0); if (trade.status === 'MATCHED') totals.matched += 1; if (['UNMATCHED', 'DISPUTED'].includes(trade.status)) totals.breaks += 1; return totals; }, { portfolioValue: 0, matched: 0, breaks: 0 }), [trades]);
  return <section><h2>Dashboard</h2><div className="stat-grid"><StatCard label="Portfolio value" value={summary.portfolioValue.toLocaleString(undefined, { style: 'currency', currency: 'USD' })} /><StatCard label="Trades streamed" value={trades.length} /><StatCard label="Matched" value={summary.matched} /><StatCard label="Open breaks" value={summary.breaks} /></div><div role="status" aria-live="polite">SSE: {isConnected ? 'connected' : 'disconnected'}</div></section>;
}
export default withAuth(Dashboard);
