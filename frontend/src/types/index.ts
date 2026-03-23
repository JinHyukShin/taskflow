// ────────────────────────────────────────────
// 공통 API 응답 타입
// ────────────────────────────────────────────

export type ApiResponse<T> = {
  success: boolean
  data: T
}

export type PageResponse<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

export type ApiError = {
  success: false
  error: {
    code: string
    message: string
    details?: Record<string, string>
  }
  timestamp: string
}

// ────────────────────────────────────────────
// Auth
// ────────────────────────────────────────────

export type LoginRequest = {
  email: string
  password: string
}

export type SignupRequest = {
  email: string
  password: string
  name: string
}

export type AuthResponse = {
  accessToken: string
  refreshToken: string
  tokenType: 'Bearer'
  expiresIn: number
}

export type UserInfo = {
  id: number
  email: string
  name: string
  createdAt: string
}

// ────────────────────────────────────────────
// Asset (자산)
// ────────────────────────────────────────────

export type AssetType = 'CRYPTO' | 'STOCK'

export type Asset = {
  id: number
  symbol: string
  name: string
  assetType: AssetType
  logoUrl: string | null
  enabled: boolean
}

export type PriceData = {
  symbol: string
  name: string
  price: number
  currency: string
  change24h: number
  changePercent24h: number
  volume24h: number
  marketCap: number
  timestamp: string
}

export type AssetWithPrice = Asset & {
  currentPrice: number | null
  change24h: number | null
  changePercent24h: number | null
  volume24h: number | null
}

// ────────────────────────────────────────────
// Candlestick / Price History
// ────────────────────────────────────────────

export type CandlestickData = {
  time: number   // Unix timestamp (seconds)
  open: number
  high: number
  low: number
  close: number
  volume: number
}

export type PeriodType = '1D' | '1W' | '1M' | '3M' | '1Y'

// ────────────────────────────────────────────
// Portfolio
// ────────────────────────────────────────────

export type Portfolio = {
  id: number
  name: string
  description: string | null
  currency: string
  createdAt: string
  updatedAt: string
}

export type PortfolioAsset = {
  symbol: string
  name: string
  quantity: number
  avgBuyPrice: number
  currentPrice: number
  currentValue: number
  pnl: number
  pnlPercent: number
  allocation: number
}

export type PortfolioSummary = {
  portfolioId: number
  name: string
  totalInvested: number
  totalCurrentValue: number
  totalUnrealizedPnl: number
  totalPnlPercent: number
  assets: PortfolioAsset[]
}

export type CreatePortfolioRequest = {
  name: string
  description?: string
  currency?: string
}

// ────────────────────────────────────────────
// Trade (거래)
// ────────────────────────────────────────────

export type TradeType = 'BUY' | 'SELL'

export type Trade = {
  tradeId: number
  symbol: string
  tradeType: TradeType
  quantity: number
  pricePerUnit: number
  totalAmount: number
  currency: string
  tradedAt: string
}

export type TradeRequest = {
  symbol: string
  tradeType: TradeType
  quantity: number
  pricePerUnit: number
  currency?: string
  tradedAt?: string
}

// ────────────────────────────────────────────
// Alert (가격 알림)
// ────────────────────────────────────────────

export type AlertCondition = 'ABOVE' | 'BELOW'
export type AlertStatus = 'ACTIVE' | 'TRIGGERED' | 'DISABLED'

export type PriceAlert = {
  id: number
  symbol: string
  assetName: string
  condition: AlertCondition
  targetPrice: number
  currency: string
  status: AlertStatus
  createdAt: string
  triggeredAt: string | null
}

export type CreateAlertRequest = {
  symbol: string
  condition: AlertCondition
  targetPrice: number
  currency?: string
}

export type AlertNotification = {
  alertId: number
  symbol: string
  assetName: string
  condition: AlertCondition
  targetPrice: number
  currentPrice: number
  currency: string
  triggeredAt: string
}

// ────────────────────────────────────────────
// Watchlist (관심종목)
// ────────────────────────────────────────────

export type Watchlist = {
  id: number
  name: string
  createdAt: string
}

export type WatchlistItem = {
  symbol: string
  name: string
  assetType: AssetType
  currentPrice: number | null
  change24h: number | null
  changePercent24h: number | null
}
