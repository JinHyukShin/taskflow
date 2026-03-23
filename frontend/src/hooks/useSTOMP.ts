import { useState, useEffect, useRef, useCallback } from 'react'
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import { JWT_STORAGE_KEY } from '@/api/client'

// ─── 타입 ─────────────────────────────────────────────────────────────────────

export type STOMPStatus = 'disconnected' | 'connecting' | 'connected' | 'error'

export type STOMPOptions = {
  /** WebSocket 엔드포인트. 기본 /ws */
  brokerURL?: string
  /** 재연결 대기 시간(ms). 기본 5000 */
  reconnectDelay?: number
  /** 연결 시 JWT 토큰을 헤더로 전달 */
  withToken?: boolean
}

export type UseSTOMPReturn = {
  status: STOMPStatus
  subscribe: <T>(destination: string, callback: (data: T) => void) => () => void
  disconnect: () => void
}

// ─── useSTOMP ─────────────────────────────────────────────────────────────────

/**
 * STOMP over WebSocket 연결 훅
 *
 * @param options STOMP 연결 옵션
 */
export function useSTOMP(options: STOMPOptions = {}): UseSTOMPReturn {
  const {
    brokerURL = `ws://${window.location.host}/ws`,
    reconnectDelay = 5000,
    withToken = true,
  } = options

  const [status, setStatus] = useState<STOMPStatus>('disconnected')
  const clientRef = useRef<Client | null>(null)
  const mountedRef = useRef(true)

  useEffect(() => {
    mountedRef.current = true

    const connectHeaders: Record<string, string> = {}
    if (withToken) {
      const token = localStorage.getItem(JWT_STORAGE_KEY)
      if (token) {
        connectHeaders['Authorization'] = `Bearer ${token}`
      }
    }

    const client = new Client({
      brokerURL,
      connectHeaders,
      reconnectDelay,
      onConnect: () => {
        if (mountedRef.current) setStatus('connected')
      },
      onDisconnect: () => {
        if (mountedRef.current) setStatus('disconnected')
      },
      onStompError: () => {
        if (mountedRef.current) setStatus('error')
      },
    })

    client.activate()
    clientRef.current = client
    setStatus('connecting')

    return () => {
      mountedRef.current = false
      client.deactivate()
      clientRef.current = null
    }
  }, [brokerURL, reconnectDelay, withToken])

  const subscribe = useCallback(<T>(destination: string, callback: (data: T) => void): (() => void) => {
    const client = clientRef.current
    if (!client || !client.connected) {
      // 연결 완료 후 구독을 위해 onConnect 시 등록 대기
      let sub: StompSubscription | null = null
      const prevOnConnect = client?.onConnect
      if (client) {
        client.onConnect = (frame) => {
          if (prevOnConnect) prevOnConnect(frame)
          sub = client.subscribe(destination, (msg: IMessage) => {
            try {
              callback(JSON.parse(msg.body) as T)
            } catch {
              // JSON 파싱 실패 시 무시
            }
          })
        }
      }
      return () => {
        sub?.unsubscribe()
      }
    }

    const sub = client.subscribe(destination, (msg: IMessage) => {
      try {
        callback(JSON.parse(msg.body) as T)
      } catch {
        // JSON 파싱 실패 시 무시
      }
    })

    return () => sub.unsubscribe()
  }, [])

  const disconnect = useCallback(() => {
    clientRef.current?.deactivate()
  }, [])

  return { status, subscribe, disconnect }
}

// ─── useSTOMPSubscription: 특정 토픽 구독 훅 ─────────────────────────────────

export type UseSTOMPSubscriptionReturn<T> = {
  data: T | null
  status: STOMPStatus
}

/**
 * STOMP 토픽을 구독하고 최신 메시지를 반환하는 훅
 *
 * @param destination 구독할 STOMP 토픽 (ex: /topic/prices/all)
 * @param options     STOMP 연결 옵션
 */
export function useSTOMPSubscription<T>(
  destination: string | null,
  options: STOMPOptions = {},
): UseSTOMPSubscriptionReturn<T> {
  const [data, setData] = useState<T | null>(null)
  const {
    brokerURL = `ws://${window.location.host}/ws`,
    reconnectDelay = 5000,
    withToken = true,
  } = options

  const [status, setStatus] = useState<STOMPStatus>('disconnected')
  const clientRef = useRef<Client | null>(null)
  const mountedRef = useRef(true)

  useEffect(() => {
    mountedRef.current = true
    if (!destination) return

    const connectHeaders: Record<string, string> = {}
    if (withToken) {
      const token = localStorage.getItem(JWT_STORAGE_KEY)
      if (token) {
        connectHeaders['Authorization'] = `Bearer ${token}`
      }
    }

    const client = new Client({
      brokerURL,
      connectHeaders,
      reconnectDelay,
      onConnect: () => {
        if (!mountedRef.current) return
        setStatus('connected')
        client.subscribe(destination, (msg: IMessage) => {
          if (!mountedRef.current) return
          try {
            setData(JSON.parse(msg.body) as T)
          } catch {
            // JSON 파싱 실패 시 무시
          }
        })
      },
      onDisconnect: () => {
        if (mountedRef.current) setStatus('disconnected')
      },
      onStompError: () => {
        if (mountedRef.current) setStatus('error')
      },
    })

    client.activate()
    clientRef.current = client
    setStatus('connecting')

    return () => {
      mountedRef.current = false
      client.deactivate()
      clientRef.current = null
    }
  }, [destination, brokerURL, reconnectDelay, withToken])

  return { data, status }
}
