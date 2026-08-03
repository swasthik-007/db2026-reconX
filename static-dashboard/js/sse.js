// TICKET-ADV107 / TICKET-ADV108 — EventSource live feed with class-based state.
(function () {
  const feedEl = document.getElementById('trade-feed');
  const statusEl = document.getElementById('sse-status');
  if (!feedEl || !statusEl) return;

  class TradeFeed {
    constructor(options) {
      this.feedEl = options.feedEl;
      this.statusEl = options.statusEl;
      this.streamUrl = options.streamUrl;
      this.maxItems = options.maxItems;
      this.sse = null;
      this.qtyFormatter = new Intl.NumberFormat('en-US');
      this.priceFormatter = new Intl.NumberFormat('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 4,
      });
    }

    updateConnectionBadge(text, variant) {
      this.statusEl.textContent = text;
      this.statusEl.className = 'status-badge status-badge--' + variant;
    }

    escapeHtml(value) {
      return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
    }

    prependTradeRow(trade) {
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
          <strong>${this.escapeHtml(trade?.tradeRef ?? 'N/A')}</strong>
          <span>[${this.escapeHtml(status)}]</span>
        </header>
        <div class="trade-card__body">
          <span>${this.escapeHtml(trade?.symbol ?? 'N/A')}</span>
          <span>qty=${this.qtyFormatter.format(Number(trade?.qty ?? 0))}</span>
          <span>price=${this.priceFormatter.format(Number(trade?.price ?? 0))}</span>
        </div>
      `;

      const placeholder = this.feedEl.querySelector('.feed-placeholder');
      if (placeholder) {
        placeholder.remove();
      }

      this.feedEl.prepend(row);
      setTimeout(function () {
        row.classList.remove('trade-card--new');
      }, 500);

      while (this.feedEl.children.length > this.maxItems) {
        this.feedEl.lastElementChild.remove();
      }
    }

    connect() {
      this.updateConnectionBadge('Connecting…', 'connecting');
      this.sse = new window.EventSource(this.streamUrl);

      this.sse.onopen = () => {
        this.updateConnectionBadge('Live', 'live');
      };

      this.sse.onmessage = (event) => {
        try {
          const trade = JSON.parse(event.data);
          this.prependTradeRow(trade);
        } catch (error) {
          console.error('Unable to parse SSE payload:', error);
        }
      };

      this.sse.onerror = () => {
        this.updateConnectionBadge('Reconnecting…', 'reconnecting');
      };
    }

    close() {
      if (this.sse) {
        this.sse.close();
      }
    }
  }

  const tradeFeed = new TradeFeed({
    feedEl: feedEl,
    statusEl: statusEl,
    streamUrl: '/api/v1/trades/stream',
    maxItems: 50,
  });

  if (typeof window.EventSource === 'undefined') {
    tradeFeed.updateConnectionBadge('SSE unsupported', 'error');
    return;
  }

  window.addEventListener('beforeunload', function () {
    tradeFeed.close();
  });

  tradeFeed.connect();
})();
