// TICKET-ADV107 — EventSource live feed with prepend + slide-in animation.
(function () {
  const FEED_EL = document.getElementById('trade-feed');
  const STATUS_EL = document.getElementById('sse-status');
  const STREAM_URL = '/api/v1/trades/stream';
  const MAX_FEED_ITEMS = 50;

  if (!FEED_EL) return;

  if (typeof window.EventSource === 'undefined') {
    updateConnectionBadge('SSE unsupported', 'error');
    return;
  }

  const qtyFormatter = new Intl.NumberFormat('en-US');
  const priceFormatter = new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 4,
  });

  let sse = null;

  function escapeHtml(value) {
    const input = String(value ?? '');
    return input
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function updateConnectionBadge(text, variant) {
    if (!STATUS_EL) return;
    STATUS_EL.textContent = text;
    STATUS_EL.className = 'status-badge status-badge--' + variant;
  }

  function prependTradeRow(trade) {
    const status = String(trade?.status ?? 'PENDING').toUpperCase();
    const statusClassMap = {
      MATCHED: 'trade-card--matched',
      BREAK: 'trade-card--break',
      UNMATCHED: 'trade-card--break',
      PENDING: '',
    };

    const row = document.createElement('article');
    row.className = ['trade-card', statusClassMap[status] || '', 'trade-card--new']
      .filter(Boolean)
      .join(' ');
    row.innerHTML = `
      <header class="trade-card__header">
        <strong>${escapeHtml(trade?.tradeRef ?? 'N/A')}</strong>
        <span>[${escapeHtml(status)}]</span>
      </header>
      <div class="trade-card__body">
        <span>${escapeHtml(trade?.symbol ?? 'N/A')}</span>
        <span>qty=${qtyFormatter.format(Number(trade?.qty ?? 0))}</span>
        <span>price=${priceFormatter.format(Number(trade?.price ?? 0))}</span>
      </div>
    `;

    const placeholder = FEED_EL.querySelector('.feed-placeholder');
    if (placeholder) {
      placeholder.remove();
    }

    FEED_EL.prepend(row);
    setTimeout(function () {
      row.classList.remove('trade-card--new');
    }, 500);

    while (FEED_EL.children.length > MAX_FEED_ITEMS) {
      FEED_EL.lastElementChild.remove();
    }
  }

  function connect() {
    updateConnectionBadge('Connecting…', 'connecting');
    sse = new window.EventSource(STREAM_URL);

    sse.onopen = function () {
      updateConnectionBadge('Live', 'live');
    };

    sse.onmessage = function (event) {
      try {
        const trade = JSON.parse(event.data);
        prependTradeRow(trade);
      } catch (error) {
        console.error('Unable to parse SSE payload:', error);
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
