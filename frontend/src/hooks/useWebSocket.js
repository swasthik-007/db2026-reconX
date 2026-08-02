// TICKET-ADV115 — useWebSocket(url) with auto-reconnect (exp backoff up to 5 tries).
import { useCallback, useEffect, useRef, useState } from 'react';

export function useWebSocket(
  url,
  { reconnect = true, maxRetries = 5, baseDelay = 500, maxDelay = 30000 } = {},
) {
  const [data, setData] = useState(null);
  const [status, setStatus] = useState('connecting');
  const wsRef = useRef(null);
  const retriesRef = useRef(0);
  const timerRef = useRef(null);
  const shouldStopRef = useRef(false);

  const connect = useCallback(() => {
    setStatus('connecting');
    const ws = new WebSocket(url);
    wsRef.current = ws;

    ws.onopen = () => {
      retriesRef.current = 0;
      setStatus('open');
    };

    ws.onmessage = (event) => {
      try {
        setData(JSON.parse(event.data));
      } catch (_error) {
        setData(event.data);
      }
    };

    ws.onerror = () => {
      setStatus('error');
    };

    ws.onclose = () => {
      setStatus('closed');
      if (!reconnect || shouldStopRef.current || retriesRef.current >= maxRetries) {
        return;
      }
      const delay = Math.min(maxDelay, baseDelay * 2 ** retriesRef.current);
      retriesRef.current += 1;
      timerRef.current = setTimeout(() => {
        connect();
      }, delay);
    };
  }, [baseDelay, maxDelay, maxRetries, reconnect, url]);

  useEffect(() => {
    shouldStopRef.current = false;
    connect();
    return () => {
      shouldStopRef.current = true;
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
      const ws = wsRef.current;
      if (ws && ws.readyState <= WebSocket.OPEN) {
        ws.close();
      }
    };
  }, [connect]);

  const send = useCallback((payload) => {
    const ws = wsRef.current;
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      return;
    }
    const message = typeof payload === 'string' ? payload : JSON.stringify(payload);
    ws.send(message);
  }, []);

  return { data, status, send };
}
