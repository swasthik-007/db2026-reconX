import React, { useCallback, useEffect, useState } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import DataTable from '@components/DataTable.jsx';
import { TradeRow } from '@components/TradeRow.jsx';
import { useDebouncedSearch } from '@hooks/useDebouncedSearch.js';
import { api } from '@services/apiService.js';
function Trades() {
  const [search, setSearch] = useState(''); const debounced = useDebouncedSearch(search, 300); const [page, setPage] = useState(0); const [data, setData] = useState({ items: [], totalPages: 0 }); const [selectedTradeId, setSelectedTradeId] = useState(null);
  const selectTrade = useCallback((id) => setSelectedTradeId(id), []);
  useEffect(() => { let active = true; const params = new URLSearchParams({ page: String(page) }); if (debounced) params.set('status', debounced); api.listTrades(params.toString()).then((response) => { if (active) setData({ items: response.content ?? response.items ?? [], totalPages: response.totalPages ?? 0 }); }).catch(() => { if (active) setData({ items: [], totalPages: 0 }); }); return () => { active = false; }; }, [debounced, page]);
  useEffect(() => setPage(0), [debounced]);
  return <section><h2>Trades</h2><input aria-label="Filter by status" placeholder="status filter (PENDING/MATCHED/…)" value={search} onChange={(event) => setSearch(event.target.value.toUpperCase())} /><DataTable data={data.items} pageSize={data.items.length || 1}><DataTable.Header columns={[{ key: 'tradeRef', label: 'Ref' }, { key: 'instrumentSymbol', label: 'Symbol' }, { key: 'quantity', label: 'Qty' }, { key: 'price', label: 'Price' }, { key: 'status', label: 'Status' }]} /><DataTable.Body render={(trade) => <TradeRow trade={trade} onClick={selectTrade} />} /><DataTable.Pagination page={page} totalPages={Math.max(1, data.totalPages)} onChange={setPage} /></DataTable>{selectedTradeId !== null && <p aria-live="polite">Selected trade: {selectedTradeId}</p>}</section>;
}
export default withAuth(Trades);
