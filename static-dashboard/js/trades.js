const trades = [
  { tradeRef: 'T-1023', symbol: 'AAPL', quantity: 180, price: 177.25, status: 'Filled' },
  { tradeRef: 'T-1024', symbol: 'MSFT', quantity: 340, price: 321.50, status: 'Pending' },
  { tradeRef: 'T-1025', symbol: 'GOOGL', quantity: 95, price: 132.80, status: 'Filled' },
  { tradeRef: 'T-1026', symbol: 'TSLA', quantity: 220, price: 843.10, status: 'Rejected' },
  { tradeRef: 'T-1027', symbol: 'AMZN', quantity: 120, price: 139.60, status: 'Pending' },
  { tradeRef: 'T-1028', symbol: 'NFLX', quantity: 135, price: 558.65, status: 'Filled' },
  { tradeRef: 'T-1029', symbol: 'NVDA', quantity: 420, price: 599.25, status: 'Filled' },
  { tradeRef: 'T-1030', symbol: 'BABA', quantity: 275, price: 82.40, status: 'Pending' },
];

const typeFormatters = {
  number: value => value.toLocaleString('en-US'),
  string: value => value,
};

const table = document.getElementById('trades-table');
const tbody = document.getElementById('trades-tbody');
const headers = Array.from(table.querySelectorAll('th[data-col]'));
let activeColumn = 'tradeRef';
let sortDirection = 'asc';
let resizing = null;
let startX = 0;
let startWidth = 0;

function renderRows(items) {
  tbody.innerHTML = '';
  items.forEach(row => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${escapeHtml(row.tradeRef)}</td>
      <td>${escapeHtml(row.symbol)}</td>
      <td>${typeFormatters.number(row.quantity)}</td>
      <td>${typeFormatters.number(row.price)}</td>
      <td>${escapeHtml(row.status)}</td>
    `;
    tbody.appendChild(tr);
  });
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function sortBy(column, type, direction) {
  const sorted = [...trades].sort((a, b) => {
    const left = a[column];
    const right = b[column];

    if (type === 'number') {
      return direction === 'asc' ? left - right : right - left;
    }

    const first = String(left).localeCompare(String(right), 'en', { sensitivity: 'base' });
    return direction === 'asc' ? first : -first;
  });

  headers.forEach(head => {
    head.classList.toggle('sort-asc', head.dataset.col === column && direction === 'asc');
    head.classList.toggle('sort-desc', head.dataset.col === column && direction === 'desc');
  });

  activeColumn = column;
  sortDirection = direction;
  renderRows(sorted);
}

function onHeaderClick(event) {
  const th = event.currentTarget;
  const column = th.dataset.col;
  const type = th.dataset.type || 'string';
  const nextDirection = th.dataset.dir === 'asc' ? 'desc' : 'asc';
  th.dataset.dir = nextDirection;
  sortBy(column, type, nextDirection);
}

function onResizeMouseDown(event) {
  const handle = event.target;
  if (!handle.classList.contains('resize-handle')) return;

  resizing = handle.closest('th');
  startX = event.clientX;
  startWidth = resizing.offsetWidth;
  document.body.classList.add('resizing');
  event.preventDefault();
}

function onResizeMouseMove(event) {
  if (!resizing) return;
  const delta = event.clientX - startX;
  resizing.style.width = `${Math.max(100, startWidth + delta)}px`;
}

function onResizeMouseUp() {
  if (!resizing) return;
  resizing = null;
  document.body.classList.remove('resizing');
}

headers.forEach(head => {
  head.addEventListener('click', onHeaderClick);
  const handle = head.querySelector('.resize-handle');
  if (handle) {
    handle.addEventListener('mousedown', onResizeMouseDown);
  }
});

window.addEventListener('mousemove', onResizeMouseMove);
window.addEventListener('mouseup', onResizeMouseUp);

sortBy(activeColumn, 'string', sortDirection);
