import { useState, useEffect, useRef, useCallback } from 'react'
import { JWT_STORAGE_KEY } from '@/api/client'

// ─── 타입 ─────────────────────────────────────────────────────────────────────

export type SSEStatus = 'connecting' | 'connected' | 'error' | 'closed'

export type SSEOptions = {
  /** 재연결 대기 시간(ms). 기본 3000 */
  retryDelay?: number
  /** 최대 재연결 횟수. 기본 5 */
  maxRetries?: number
  /** 연결 시 JWT 토큰을 쿼리 파라미터로 전달할지 여부 */
  withToken?: boolean
}

export type UseSSEReturn<T> = {
  data: T | null
  status: SSEStatus
  error: string | null
  disconnect: () => void
  reconnect: () => void
}

// ─── useSSE ───────────────────────────────────────────────────────────────────

/**
 * SSE(Server-Sent Events) 연결 커스텀 훅
 *
 * @param url       SSE 엔드포인트 URL (null 이면 연결 안 함)
 * @param eventName 수신할 이벤트 이름. 기본 'message'
 * @param options   재연결 옵션
 */
export function useSSE<T>(
  url: string | null,
  eventName: string = 'message',
  options: SSEOptions = {},
): UseSSEReturn<T> {
  const { retryDelay = 3000, maxRetries = 5, withToken = true } = options

  const [data, setData] = useState<T | null>(null)
  const [status, setStatus] = useState<SSEStatus>('closed')
  const [error, setError] = useState<string | null>(null)

  const esRef = useRef<EventSource | null>(null)
  const retryCountRef = useRef(0)
  const retryTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const mountedRef = useRef(true)

  const clearRetryTimer = () => {
    if (retryTimerRef.current) {
      clearTimeout(retryTimerRef.current)
      retryTimerRef.current = null
    }
  }

  const close = useCallback(() => {
    clearRetryTimer()
    if (esRef.current) {
      esRef.current.close()
      esRef.current = null
    }
    if (mountedRef.current) setStatus('closed')
  }, [])

  const connect = useCallback(() => {
    if (!url) return

    if (esRef.current) {
      esRef.current.close()
      esRef.current = null
    }

    let fullUrl = url
    if (withToken) {
      const token = localStorage.getItem(JWT_STORAGE_KEY)
      if (token) {
        const separator = url.includes('?') ? '&' : '?'
        fullUrl = `${url}${separator}token=${encodeURIComponent(token)}`
      }
    }

    if (mountedRef.current) {
      setStatus('connecting')
      setError(null)
    }

    const es = new EventSource(fullUrl)
    esRef.current = es

    es.addEventListener(eventName, (e: MessageEvent) => {
      if (!mountedRef.current) return
      try {
        const parsed = JSON.parse(e.data) as T
        setData(parsed)
        setStatus('connected')
        retryCountRef.current = 0
      } catch {
        setData(e.data as unknown as T)
      }
    })

    es.addEventListener('open', () => {
      if (mountedRef.current) {
        setStatus('connected')
        setError(null)
        retryCountRef.current = 0
      }
    })

    es.addEventListener('error', () => {
      if (!mountedRef.current) return
      es.close()
      esRef.current = null
      setStatus('error')

      if (retryCountRef.current < maxRetries) {
        retryCountRef.current += 1
        setError(`연결 오류. ${retryDelay / 1000}초 후 재시도... (${retryCountRef.current}/${maxRetries})`)
        retryTimerRef.current = setTimeout(() => {
          if (mountedRef.current) connect()
        }, retryDelay)
      } else {
        setError('SSE 연결에 실패했습니다. 페이지를 새로고침 해주세요.')
      }
    })
  }, [url, eventName, withToken, retryDelay, maxRetries]) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    mountedRef.current = true
    if (url) {
      retryCountRef.current = 0
      connect()
    }
    return () => {
      mountedRef.current = false
      clearRetryTimer()
      if (esRef.current) {
        esRef.current.close()
        esRef.current = null
      }
    }
  }, [url]) // connect 를 deps 에서 제외해 무한 루프 방지

  return {
    data,
    status,
    error,
    disconnect: close,
    reconnect: () => {
      retryCountRef.current = 0
      connect()
    },
  }
}
