// TICKET-ADV104 / TICKET-ADV105 — EventSource live feed with prepend + slide-in animation.
(function () {
  const feed = document.getElementById('trade-feed');
  const statusBadge = document.getElementById('sse-status');

  if (!feed || !statusBadge) return;

  const STREAM_URL = 'http://localhost:8080/api/v1/trades/stream';
  let sse = null;

  function updateConnectionBadge(text, variant) {
    statusBadge.textContent = text;
    statusBadge.className = 'sse-status';

    if (variant === 'live') {
      statusBadge.classList.add('is-live');
    } else if (variant === 'reconnecting') {
      statusBadge.classList.add('is-reconnecting');
    }
  }

  const demoEvents = [
    { tradeRef: 'EQU-20260603-0001', symbol: 'SAP.DE', qty: 1000, price: 125.50, status: 'MATCHED' },
    { tradeRef: 'FX-20260603-0001', symbol: 'EUR/USD', qty: 1_000_000, price: 1.0852, status: 'PENDING' },
    { tradeRef: 'EQU-20260603-0002', symbol: 'AAPL', qty: 500, price: 178.20, status: 'BREAK' },
  ];

  function escapeHtml(value) {
    return String(value || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function formatQty(value) {
    return new Intl.NumberFormat('en-US').format(value ?? 0);
  }

  function formatPrice(value) {
    return new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 4,
    }).format(value ?? 0);
  }

  function statusModifier(status) {
    const normalized = String(status || '').toUpperCase();
    if (normalized === 'MATCHED') {
      return 'trade-card--matched';
    }
    if (normalized === 'UNMATCHED' || normalized === 'BREAK') {
      return 'trade-card--break';
    }
    return '';
  }

  function prependTradeRow(trade) {
    const row = document.createElement('article');
    const statusClass = statusModifier(trade.status);
    row.className = `trade-card ${statusClass} trade-card--new`;

    const tradeRef = escapeHtml(trade.tradeRef || 'Trade');
    const symbol = escapeHtml(trade.symbol || 'Unknown');
    const status = escapeHtml(trade.status || 'PENDING');
    const qty = formatQty(trade.qty);
    const price = formatPrice(trade.price);
    const currency = trade.currency ? escapeHtml(trade.currency) : '';

    row.innerHTML = `
      <header class="trade-card__header">
        <strong>${tradeRef}</strong>
        <span>${status}</span>
      </header>
      <div class="trade-card__body">
        <span>${symbol}</span>
        <span>qty=${qty}</span>
        <span>price=${price}${currency ? ' ' + currency : ''}</span>
      </div>`;

    feed.prepend(row);

    setTimeout(() => {
      row.classList.remove('trade-card--new');
    }, 500);

    while (feed.children.length > 50) {
      feed.lastElementChild.remove();
    }
  }

  function startDemoFeed() {
    demoEvents.forEach((event, index) => {
      setTimeout(() => prependTradeRow(event), 500 * index);
    });
  }

  function connect() {
    updateConnectionBadge('Connecting…', '');
    startDemoFeed();

    if (typeof window.EventSource === 'undefined') {
      updateConnectionBadge('Live', 'live');
      return;
    }

    sse = new EventSource(STREAM_URL);

    sse.onopen = function () {
      updateConnectionBadge('Live', 'live');
    };

    sse.onmessage = function (event) {
      try {
        const trade = JSON.parse(event.data);
        prependTradeRow(trade);
      } catch (error) {
        console.warn('Unable to parse trade event', error);
      }
    };

    sse.onerror = function () {
      updateConnectionBadge('Reconnecting…', 'reconnecting');
    };
  }

  window.addEventListener('beforeunload', function () {
    if (sse) {
      sse.close();
    }
  });

  connect();
})();
