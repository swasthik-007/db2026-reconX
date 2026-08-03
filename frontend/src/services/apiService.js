const BASE = '/api';
function authHeaders() { const token = sessionStorage.getItem('reconx-token'); return token ? { Authorization: `Bearer ${token}` } : {}; }
async function request(method, path, body) {
  const response = await fetch(`${BASE}${path}`, { method, headers: { 'Content-Type': 'application/json', ...authHeaders() }, ...(body === undefined ? {} : { body: JSON.stringify(body) }) });
  if (!response.ok) { let detail = response.statusText; try { const payload = await response.json(); detail = payload.message || payload.detail || JSON.stringify(payload); } catch { /* retain status */ } throw new Error(`HTTP ${response.status}: ${detail}`); }
  return response.status === 204 ? null : response.json();
}
export const api = {
  login: (email, password) => request('POST', '/auth/login', { email, password }),
  listTrades: (params = '') => request('GET', `/v1/trades${params ? `?${String(params).replace(/^\?/, '')}` : ''}`),
  createTrade: (trade) => request('POST', '/v1/trades', trade), updateStatus: (id, status) => request('PATCH', `/v1/trades/${id}/status`, { status }), deleteTrade: (id) => request('DELETE', `/v1/trades/${id}`),
  runRecon: (body) => request('POST', '/v1/recon/run', body), reconResults: (jobId) => request('GET', `/v1/recon/jobs/${jobId}/results`), audit: (tradeRef) => request('GET', `/v1/audit/trades/${encodeURIComponent(tradeRef)}`),
};
