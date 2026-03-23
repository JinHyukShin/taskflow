# StockPulse

실시간 주식 및 암호화폐 대시보드. WebSocket(STOMP) 기반 가격 스트리밍, 포트폴리오 손익 추적, 캔들스틱 차트, SSE 가격 알림을 제공하는 풀스택 웹 애플리케이션입니다.

---

## 기술 스택

### Backend

| 분류 | 기술 |
|---|---|
| Language / Runtime | Java 25 |
| Framework | Spring Boot 3.4.4 |
| WebSocket | Spring WebSocket (STOMP + SockJS) |
| Realtime Push | SSE (Server-Sent Events) |
| Database | PostgreSQL 17 |
| Cache | Redis 7.4 (TTL 15s / stale 5m) |
| HTTP Client | Spring WebFlux WebClient |
| Security | Spring Security + JWT (JJWT 0.12.6) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Gradle Kotlin DSL |

### Frontend

| 분류 | 기술 |
|---|---|
| Framework | React 19 + TypeScript |
| Build | Vite 6 |
| Styling | Tailwind CSS 4 |
| STOMP Client | @stomp/stompjs 7 |
| Charts | lightweight-charts 4 (캔들스틱), Recharts 2 (파이/라인) |
| HTTP Client | Axios |
| Router | React Router 7 |

### Infrastructure

| 분류 | 기술 |
|---|---|
| Container | Docker + Docker Compose |
| DB Migration | Flyway (V1\_\_init.sql, V2\_\_initial\_data.sql) |

---

## 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────────────┐
│                        React Frontend (Vite)                        │
│                                                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐  │
│  │ DashboardPage│  │ PortfolioPage│  │      AlertsPage (SSE)    │  │
│  └──────┬───────┘  └──────┬───────┘  └────────────┬─────────────┘  │
│         │                 │                        │                │
│  useSTOMP (STOMP/WS)  useFetch (REST)         useSSE (EventSource) │
└─────────┼─────────────────┼────────────────────────┼───────────────┘
          │                 │                        │
          │ ws://host/ws    │ HTTP /api/v1/**         │ GET /api/v1/alerts/stream
          │ (SockJS)        │                        │ (text/event-stream)
          │                 │                        │
┌─────────▼─────────────────▼────────────────────────▼───────────────┐
│                    Spring Boot Application (:8080)                  │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │           WebSocketConfig  (STOMP Message Broker)           │   │
│  │   endpoint: /ws  |  broker: /topic, /queue  |  app: /app   │   │
│  └──────────────────────────┬──────────────────────────────────┘   │
│                             │                                       │
│  ┌──────────────────────────▼──────────────────────────────────┐   │
│  │              PriceStreamScheduler (@Scheduled 5s)           │   │
│  │                                                             │   │
│  │  1. AssetRepository → active assets (STOCK / CRYPTO)       │   │
│  │  2. ExternalPriceService → CoinGecko(batch) / Yahoo(단건)  │   │
│  │  3. PriceCacheService.setPrice() → Redis (TTL 15s)          │   │
│  │  4. StompPricePublisher.publishAll() → /topic/prices/all    │   │
│  │     StompPricePublisher.publish()   → /topic/prices/{sym}   │   │
│  │  5. AlertService.checkAlerts() → SseEmitter push            │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  REST Controllers: Auth / Asset / Portfolio / Trade /               │
│                    Watchlist / Alert / PriceHistory                 │
└──────────────────────────┬───────────────────────┬─────────────────┘
                           │                       │
                  ┌────────▼───────┐     ┌─────────▼──────┐
                  │  PostgreSQL 17 │     │   Redis 7.4    │
                  │  (JPA/Flyway)  │     │  price cache   │
                  └────────────────┘     └────────────────┘
                           │
              ┌────────────┴────────────┐
              │  External Price APIs    │
              │  CoinGecko API  (crypto)│
              │  Yahoo Finance  (stock) │
              └─────────────────────────┘
```

### WebSocket STOMP 흐름

```
Client                               Server (Spring STOMP Broker)
  │                                          │
  │──── CONNECT (Authorization: Bearer JWT) ──▶│  JwtChannelInterceptor 인증
  │◀─── CONNECTED ──────────────────────────── │
  │                                          │
  │──── SUBSCRIBE /topic/prices/all ─────────▶│
  │──── SUBSCRIBE /topic/prices/BTC ─────────▶│
  │                                          │
  │         [매 5초 PriceStreamScheduler]      │
  │◀─── MESSAGE /topic/prices/all ([]PriceData)│
  │◀─── MESSAGE /topic/prices/BTC (PriceData) │
  │                                          │
  │──── DISCONNECT ──────────────────────────▶│
```

---

## 주요 기능

### 실시간 가격 스트리밍
- 5초 간격으로 STOMP 브로드캐스트 (`/topic/prices/all`, `/topic/prices/{symbol}`)
- CoinGecko API로 암호화폐 배치 조회, Yahoo Finance API로 주식 개별 조회
- Redis 캐시 우선 조회 (TTL 15초), 만료 시 stale 폴백 (5분)

### 포트폴리오 손익 관리
- 포트폴리오 생성/삭제 및 종목별 매수/매도 거래 기록
- 평균단가 기반 실시간 수익률(%) 및 평가손익(KRW/USD) 계산
- 포트폴리오 요약(`/portfolios/{id}/summary`): 총 투자금액, 평가금액, 손익

### 캔들스틱 차트
- `lightweight-charts` 기반 OHLC 캔들스틱 렌더링
- 기간(1M/3M/6M/1Y) 및 interval(1d/1w) 파라미터 지원
- PriceHistory 엔티티에서 집계한 데이터 제공

### 가격 알림 (SSE)
- 조건(ABOVE / BELOW) 설정 후 SSE 스트림 구독 (`/api/v1/alerts/stream`)
- PriceStreamScheduler가 가격 갱신 시마다 알림 조건 체크 → SseEmitter push
- 알림 상태: ACTIVE → TRIGGERED 자동 전환

### 관심 종목 (Watchlist)
- 관심 목록 생성 및 종목 추가/제거
- 관심 종목 실시간 가격을 STOMP로 구독

---

## API 엔드포인트

### 인증 (`/api/v1`)

| Method | Path | 설명 | 인증 필요 |
|--------|------|------|-----------|
| POST | `/auth/signup` | 회원가입 (AccessToken + RefreshToken 반환) | No |
| POST | `/auth/login` | 로그인 | No |
| POST | `/auth/refresh` | AccessToken 갱신 | No |
| POST | `/auth/logout` | 로그아웃 (RefreshToken 무효화) | No |
| GET | `/users/me` | 내 정보 조회 | Yes |

### 자산 (`/api/v1/assets`)

| Method | Path | 설명 | 인증 필요 |
|--------|------|------|-----------|
| GET | `/assets` | 전체 자산 목록 (`?type=STOCK\|CRYPTO`) | No |
| GET | `/assets/{symbol}` | 자산 상세 | No |
| GET | `/assets/{symbol}/price` | 현재 가격 조회 (Redis 캐시) | No |
| GET | `/assets/prices?symbols=BTC,ETH` | 복수 자산 가격 조회 | No |
| GET | `/assets/{symbol}/history?period=1M` | 가격 히스토리 (캔들스틱용) | No |
| GET | `/assets/{symbol}/candles?interval=1d` | OHLC 캔들 데이터 | No |

### 포트폴리오 (`/api/v1/portfolios`)

| Method | Path | 설명 | 인증 필요 |
|--------|------|------|-----------|
| POST | `/portfolios` | 포트폴리오 생성 | Yes |
| GET | `/portfolios` | 내 포트폴리오 목록 | Yes |
| GET | `/portfolios/{id}` | 포트폴리오 상세 | Yes |
| GET | `/portfolios/{id}/summary` | 손익 요약 (실시간 가격 반영) | Yes |
| DELETE | `/portfolios/{id}` | 포트폴리오 삭제 | Yes |
| POST | `/portfolios/{id}/trades` | 거래 실행 (BUY/SELL) | Yes |
| GET | `/portfolios/{id}/trades` | 거래 내역 (페이징) | Yes |

### 관심 목록 (`/api/v1/watchlists`)

| Method | Path | 설명 | 인증 필요 |
|--------|------|------|-----------|
| POST | `/watchlists` | 관심 목록 생성 | Yes |
| GET | `/watchlists` | 내 관심 목록 전체 조회 | Yes |
| POST | `/watchlists/{id}/items` | 종목 추가 | Yes |
| DELETE | `/watchlists/{id}/items/{symbol}` | 종목 제거 | Yes |

### 가격 알림 (`/api/v1/alerts`)

| Method | Path | 설명 | 인증 필요 |
|--------|------|------|-----------|
| POST | `/alerts` | 알림 생성 (ABOVE/BELOW 조건) | Yes |
| GET | `/alerts` | 내 알림 목록 | Yes |
| PUT | `/alerts/{id}` | 알림 수정 | Yes |
| DELETE | `/alerts/{id}` | 알림 삭제 | Yes |
| GET | `/alerts/stream` | SSE 스트림 구독 | Yes |

---

## WebSocket STOMP 구독 경로

| 구독 경로 | 페이로드 타입 | 설명 |
|-----------|--------------|------|
| `/topic/prices/all` | `List<PriceData>` | 전체 자산 가격 일괄 업데이트 (5초 주기) |
| `/topic/prices/{symbol}` | `PriceData` | 특정 종목 가격 업데이트 (예: `/topic/prices/BTC`) |

**STOMP 접속 엔드포인트**: `ws://localhost:8080/ws` (SockJS 호환)

**인증**: STOMP CONNECT 프레임 헤더에 `Authorization: Bearer {accessToken}` 포함

```javascript
// 프론트엔드 연결 예시
const client = new Client({
  brokerURL: 'ws://localhost:8080/ws/websocket',
  connectHeaders: { Authorization: `Bearer ${token}` },
  onConnect: () => {
    client.subscribe('/topic/prices/all', (msg) => {
      const prices = JSON.parse(msg.body); // PriceData[]
    });
  },
});
client.activate();
```

---

## 실행 방법 (Docker Compose)

### 사전 요구사항
- Docker 24+
- Docker Compose 2.20+

### 1. 환경 변수 설정

```bash
cp docker/.env.example docker/.env
# docker/.env 파일을 열어 JWT_SECRET 등 민감 값 수정
```

### 2. 전체 스택 실행

```bash
# 이미지 빌드 후 전체 서비스 기동 (app + postgres + redis)
docker compose -f docker/docker-compose.yml up --build -d
```

### 3. 동작 확인

```bash
# 헬스체크
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

### 4. 서비스 종료

```bash
docker compose -f docker/docker-compose.yml down
```

### 서비스 포트 기본값

| 서비스 | 포트 | 환경 변수 |
|--------|------|-----------|
| Spring Boot App | 8080 | `APP_PORT` |
| PostgreSQL | 5432 | `POSTGRES_PORT` |
| Redis | 6379 | `REDIS_PORT` |

### 로컬 개발 환경 (Docker 없이)

```bash
# 인프라만 기동 (PostgreSQL + Redis)
./scripts/start-infra.sh

# 백엔드 실행
./scripts/run.sh

# 프론트엔드 실행
cd frontend && npm install && npm run dev
# http://localhost:5173
```

---

## 프로젝트 구조

```
stockpulse/
├── src/main/java/com/stockpulse/
│   ├── domain/
│   │   ├── alert/          # 가격 알림 (SSE)
│   │   ├── asset/          # 자산 정보 + 현재가 조회
│   │   ├── auth/           # 회원가입/로그인/JWT
│   │   ├── portfolio/      # 포트폴리오 + 손익 계산
│   │   ├── pricehistory/   # OHLC 히스토리 (캔들스틱)
│   │   ├── trade/          # 매수/매도 거래
│   │   └── watchlist/      # 관심 종목
│   ├── global/
│   │   ├── config/         # WebSocket, Security, Redis, Swagger 설정
│   │   ├── exception/      # BusinessException, ErrorCode, GlobalExceptionHandler
│   │   └── security/       # JwtProvider, JwtAuthenticationFilter, JwtChannelInterceptor
│   └── infra/
│       ├── external/       # CoinGeckoClient, YahooFinanceClient, ExternalPriceService
│       ├── redis/          # PriceCacheService (TTL), RateLimitService
│       └── websocket/      # PriceStreamScheduler, StompPricePublisher
├── frontend/
│   └── src/
│       ├── components/     # charts/, layout/, ui/
│       ├── hooks/          # useSTOMP, useSSE, useFetch
│       └── pages/          # Dashboard, Markets, Portfolio, Alerts, Watchlist
├── database/migration/     # Flyway SQL 마이그레이션
├── docker/                 # docker-compose.yml, .env.example
└── scripts/                # build, run, start/stop-infra 스크립트
```
