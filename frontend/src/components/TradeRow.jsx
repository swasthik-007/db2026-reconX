import React from 'react';
function TradeRowImpl({ trade, onClick }) { return <button className="trade-row" type="button" onClick={() => onClick(trade.id)}><span>{trade.tradeRef}</span><span>{trade.instrumentSymbol ?? trade.symbol ?? '—'}</span><span>{trade.quantity ?? trade.qty}</span><span>{trade.price}</span><span>{trade.status}</span></button>; }
function areEqual(previous, next) { return previous.trade.id === next.trade.id && previous.trade.status === next.trade.status && previous.trade.price === next.trade.price && previous.onClick === next.onClick; }
export const TradeRow = React.memo(TradeRowImpl, areEqual);
