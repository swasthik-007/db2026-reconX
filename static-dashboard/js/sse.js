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

  function prepend(trade) {
    const el = document.createElement('article');
    const normalizedStatus = String(trade.status || 'PENDING').toLowerCase();
    el.className = 'trade-card trade-card--' + normalizedStatus;
    el.innerHTML = `
      <strong>${trade.tradeRef || 'Trade'}</strong>
      <span> ${trade.symbol || 'Unknown'} </span>
      <span> qty=${trade.qty || 0} </span>
      <span> price=${trade.price || 0} </span>
      <span> [${trade.status || 'PENDING'}]</span>`;
    feed.prepend(el);
  }

  function startDemoFeed() {
    demoEvents.forEach((event, index) => {
      setTimeout(() => prepend(event), 500 * index);
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
        prepend(trade);
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
