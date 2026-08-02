// TICKET-ADV116 — useTradeStream() — SSE subscription returning live trades.
import { useEffect, useState } from 'react';

const MAX_BUFFER = 200;

export function useTradeStream(url = '/api/v1/trades/stream') {
  const [trades, setTrades] = useState([]);
  const [isConnected, setConnected] = useState(false);

  useEffect(() => {
    const sse = new EventSource(url);

    const pushTrade = (trade) => {
      setTrades((prev) => [trade, ...prev].slice(0, MAX_BUFFER));
    };

    const handleMessage = (event) => {
      try {
        pushTrade(JSON.parse(event.data));
      } catch (_error) {
        // Ignore malformed payloads from non-trade events.
      }
    };

    const handleMatched = (event) => {
      try {
        const update = JSON.parse(event.data);
        setTrades((prev) =>
          prev.map((trade) => {
            const sameTrade =
              (update.tradeRef && trade.tradeRef === update.tradeRef) ||
              (update.id && trade.id === update.id);
            if (!sameTrade) return trade;
            return { ...trade, ...update };
          }),
        );
      } catch (_error) {
        // Ignore malformed payloads from named events.
      }
    };

    sse.onopen = () => setConnected(true);
    sse.onerror = () => setConnected(false);
    sse.onmessage = handleMessage;
    sse.addEventListener('trade-matched', handleMatched);

    return () => {
      sse.removeEventListener('trade-matched', handleMatched);
      sse.close();
    };
  }, [url]);

  return { trades, isConnected };
}
