// TICKET-ADV114 — Compound <DataTable> with Header / Body / Pagination subcomponents.
import React, { createContext, useCallback, useContext, useMemo, useState } from 'react';

const DataTableContext = createContext(null);

function useDataTable() {
  const context = useContext(DataTableContext);
  if (!context) {
    throw new Error('DataTable sub-components must be used inside <DataTable>.');
  }
  return context;
}

export default function DataTable({ children, data = [], pageSize = 20, sort = null, onSortChange }) {
  const [sortKey, setSortKey] = useState(sort);
  const [sortDir, setSortDir] = useState('ascending');
  const [page, setPage] = useState(0);

  const sortedRows = useMemo(() => {
    if (!sortKey) return data;
    const next = [...data];
    next.sort((a, b) => {
      const av = a?.[sortKey];
      const bv = b?.[sortKey];
      const numA = Number(av);
      const numB = Number(bv);
      const isNumber = Number.isFinite(numA) && Number.isFinite(numB);
      if (isNumber) {
        return sortDir === 'ascending' ? numA - numB : numB - numA;
      }
      const cmp = String(av ?? '').localeCompare(String(bv ?? ''));
      return sortDir === 'ascending' ? cmp : -cmp;
    });
    return next;
  }, [data, sortDir, sortKey]);

  const totalPages = useMemo(
    () => Math.max(1, Math.ceil(sortedRows.length / pageSize)),
    [pageSize, sortedRows.length],
  );

  const pagedRows = useMemo(() => {
    const start = page * pageSize;
    return sortedRows.slice(start, start + pageSize);
  }, [page, pageSize, sortedRows]);

  const onHeaderSort = useCallback((key) => {
    const nextDir = sortKey === key && sortDir === 'ascending' ? 'descending' : 'ascending';
    setSortKey(key);
    setSortDir(nextDir);
    setPage(0);
    if (onSortChange) {
      onSortChange(key);
    }
  }, [onSortChange, sortDir, sortKey]);

  const contextValue = useMemo(() => ({
    rows: pagedRows,
    page,
    totalPages,
    sortKey,
    sortDir,
    setPage,
    onHeaderSort,
  }), [onHeaderSort, page, pagedRows, sortDir, sortKey, totalPages]);

  return (
    <DataTableContext.Provider value={contextValue}>
      <div className="data-table">{children}</div>
    </DataTableContext.Provider>
  );
}

DataTable.Header = function Header({ columns }) {
  const { sortKey, onHeaderSort } = useDataTable();

  return (
    <div className="data-table__header" role="row">
      {columns.map((column) => (
        <button
          key={column.key}
          type="button"
          className={`data-table__th data-table__th--${sortKey === column.key ? 'active' : 'idle'}`}
          onClick={() => onHeaderSort(column.key)}
        >
          {column.label}
        </button>
      ))}
    </div>
  );
};

DataTable.Body = function Body({ rows, render }) {
  const { rows: contextRows } = useDataTable();
  const sourceRows = rows ?? contextRows;

  return (
    <div className="data-table__body">
      {sourceRows.map((row, index) => (
        <div key={row.id ?? row.tradeRef ?? index} className="data-table__row" role="row">
          {render(row)}
        </div>
      ))}
    </div>
  );
};

DataTable.Pagination = function Pagination({ page, totalPages, onChange }) {
  const context = useDataTable();
  const currentPage = typeof page === 'number' ? page : context.page;
  const pages = typeof totalPages === 'number' ? totalPages : context.totalPages;
  const change = onChange || context.setPage;

  return (
    <nav className="data-table__pagination" aria-label="Pagination">
      <button
        type="button"
        disabled={currentPage === 0}
        onClick={() => change(currentPage - 1)}
      >
        ‹
      </button>
      <span>{currentPage + 1} / {pages}</span>
      <button
        type="button"
        disabled={currentPage >= pages - 1}
        onClick={() => change(currentPage + 1)}
      >
        ›
      </button>
    </nav>
  );
};
