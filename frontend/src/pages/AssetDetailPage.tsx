import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useSTOMPSubscription } from '@/hooks/useSTOMP'
import { useFetch } from '@/hooks/useFetch'
import { CandlestickChart } from '@/components/charts/CandlestickChart'
import { Card } from '@/components/ui/Card'
import { AssetTypeBadge } from '@/components/ui/Badge'
import { Modal } from '@/components/ui/Modal'
import { useApiCall } from '@/hooks/useFetch'
import type {
  ApiResponse,
  Asset,
  PriceData,
  CandlestickData,
  PeriodType,
  TradeRequest,
  TradeType,
  PageResponse,
  Portfolio,
} from '@/types'

// ─── 기간 선택 버튼 ───────────────────────────────────────────────────────────

const PERIODS: PeriodType[] = ['1D', '1W', '1M', '3M', '1Y']

// ─── 빠른 매수/매도 모달 ──────────────────────────────────────────────────────

type QuickTradeModalProps = {
  open: boolean
  onClose: () => void
  symbol: string
  currentPrice: number | null
  defaultType: TradeType
}

function QuickTradeModal({ open, onClose, symbol, currentPrice, defaultType }: QuickTradeModalProps) {
  const [portfolioId, setPortfolioId] = useState<number | null>(null)
  const [quantity, setQuantity] = useState('')
  const [price, setPrice] = useState(currentPrice?.toString() ?? '')

  const { data: portfoliosRes } = useFetch<ApiResponse<PageResponse<Portfolio>>>('/portfolios')
  const portfolios = portfoliosRes?.data?.content ?? []

  useEffect(() => {
    if (portfolios.length > 0 && portfolioId === null) {
      setPortfolioId(portfolios[0].id)
    }
  }, [portfolios, portfolioId])

  useEffect(() => {
    if (currentPrice != null) setPrice(currentPrice.toString())
  }, [currentPrice])

  const { execute, loading, error } = useApiCall<unknown, TradeRequest>(
    portfolioId ? `/portfolios/${portfolioId}/trades` : '',
    'POST',
  )

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!portfolioId) return
    const result = await execute({
      symbol,
      tradeType: defaultType,
      quantity: parseFloat(quantity),
      pricePerUnit: parseFloat(price),
      currency: 'USD',
    })
    if (result !== null) onClose()
  }

  const totalAmount = parseFloat(quantity || '0') * parseFloat(price || '0')

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={`${symbol} ${defaultType === 'BUY' ? '매수' : '매도'}`}
      footer={
        <>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
          >
            취소
          </button>
          <button
            type="submit"
            form="quick-trade-form"
            disabled={loading || !portfolioId}
            className={`rounded-lg px-4 py-2 text-sm font-medium text-white transition-colors disabled:opacity-50 ${
              defaultType === 'BUY' ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700'
            }`}
          >
            {loading ? '처리 중...' : defaultType === 'BUY' ? '매수 확인' : '매도 확인'}
          </button>
        </>
      }
    >
      <form id="quick-trade-form" onSubmit={handleSubmit} className="space-y-4">
        {portfolios.length > 0 && (
          <div>
            <label htmlFor="qt-portfolio" className="block text-sm font-medium text-gray-700 mb-1">
              포트폴리오
            </label>
            <select
              id="qt-portfolio"
              value={portfolioId ?? ''}
              onChange={(e) => setPortfolioId(Number(e.target.value))}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {portfolios.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </div>
        )}

        <div>
          <label htmlFor="qt-qty" className="block text-sm font-medium text-gray-700 mb-1">수량</label>
          <input
            id="qt-qty"
            type="number"
            required
            min="0"
            step="any"
            placeholder="0"
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label htmlFor="qt-price" className="block text-sm font-medium text-gray-700 mb-1">단가 (USD)</label>
          <input
            id="qt-price"
            type="number"
            required
            min="0"
            step="any"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {totalAmount > 0 && (
          <div className="rounded-lg bg-gray-50 p-3">
            <p className="text-sm text-gray-600">
              총 금액:{' '}
              <span className="font-semibold text-gray-900">
                ${totalAmount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </span>
            </p>
          </div>
        )}

        {error && <p className="text-sm text-red-500">{error}</p>}
      </form>
    </Modal>
  )
}

// ─── AssetDetailPage ──────────────────────────────────────────────────────────

function AssetDetailPage() {
  const { symbol } = useParams<{ symbol: string }>()
  const [period, setPeriod] = useState<PeriodType>('1M')
  const [tradeModal, setTradeModal] = useState<{ open: boolean; type: TradeType }>({ open: false, type: 'BUY' })
  const [livePrice, setLivePrice] = useState<PriceData | null>(null)

  // STOMP: 특정 종목 가격 구독
  const { data: priceMessage } = useSTOMPSubscription<PriceData>(
    symbol ? `/topic/prices/${symbol}` : null,
  )

  useEffect(() => {
    if (priceMessage) setLivePrice(priceMessage)
  }, [priceMessage])

  // 자산 정보
  const { data: assetRes, loading: assetLoading } = useFetch<ApiResponse<Asset>>(
    symbol ? `/assets/${symbol}` : null,
    undefined,
    [symbol],
  )

  // 캔들스틱 데이터
  const { data: candlesRes, loading: candlesLoading } = useFetch<ApiResponse<CandlestickData[]>>(
    symbol ? `/assets/${symbol}/candles?interval=1d&period=${period}` : null,
    undefined,
    [symbol, period],
  )

  const asset = assetRes?.data
  const candles = candlesRes?.data ?? []

  if (!symbol) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500">심볼을 찾을 수 없습니다.</p>
        <Link to="/markets" className="text-blue-600 hover:underline text-sm mt-2 inline-block">
          마켓으로 돌아가기
        </Link>
      </div>
    )
  }

  const currentPrice = livePrice?.price ?? null
  const changePercent = livePrice?.changePercent24h ?? null
  const isPositive = (changePercent ?? 0) >= 0

  return (
    <div className="space-y-5">
      {/* 헤더: 현재가 + 변동 */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-3 mb-1">
            {assetLoading ? (
              <div className="h-8 w-32 animate-pulse rounded bg-gray-200" />
            ) : (
              <>
                <h2 className="text-2xl font-bold text-gray-900">{symbol}</h2>
                {asset && <AssetTypeBadge type={asset.assetType} />}
                {asset && <span className="text-gray-500 text-sm">{asset.name}</span>}
              </>
            )}
          </div>

          {currentPrice != null ? (
            <div className="flex items-baseline gap-3">
              <span className="text-4xl font-bold text-gray-900 tabular-nums">
                ${currentPrice.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </span>
              {changePercent != null && (
                <span className={`text-lg font-medium ${isPositive ? 'text-green-600' : 'text-red-500'}`}>
                  {isPositive ? '+' : ''}{changePercent.toFixed(2)}%
                </span>
              )}
              <div className="flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse inline-block" />
                <span className="text-xs text-gray-400">Live</span>
              </div>
            </div>
          ) : (
            <div className="h-10 w-48 animate-pulse rounded bg-gray-200" />
          )}
        </div>

        {/* 매수/매도 버튼 */}
        <div className="flex gap-2">
          <button
            type="button"
            onClick={() => setTradeModal({ open: true, type: 'BUY' })}
            className="rounded-lg bg-green-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-green-700 transition-colors"
          >
            매수
          </button>
          <button
            type="button"
            onClick={() => setTradeModal({ open: true, type: 'SELL' })}
            className="rounded-lg bg-red-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-red-700 transition-colors"
          >
            매도
          </button>
        </div>
      </div>

      {/* 캔들스틱 차트 */}
      <Card padding={false}>
        <div className="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-gray-700">가격 차트</h3>
          {/* 기간 선택 */}
          <div className="flex items-center gap-1">
            {PERIODS.map((p) => (
              <button
                key={p}
                type="button"
                onClick={() => setPeriod(p)}
                className={`px-3 py-1 rounded text-xs font-medium transition-colors ${
                  period === p
                    ? 'bg-blue-600 text-white'
                    : 'text-gray-500 hover:bg-gray-100'
                }`}
              >
                {p}
              </button>
            ))}
          </div>
        </div>
        <div className="p-4">
          {candlesLoading ? (
            <div className="h-96 flex items-center justify-center">
              <div className="text-sm text-gray-400">차트 로딩 중...</div>
            </div>
          ) : candles.length === 0 ? (
            <div className="h-96 flex items-center justify-center">
              <p className="text-sm text-gray-400">차트 데이터가 없습니다.</p>
            </div>
          ) : (
            <CandlestickChart data={candles} height={400} />
          )}
        </div>
      </Card>

      {/* 추가 정보 */}
      {livePrice && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {[
            { label: '24h 거래량', value: `$${(livePrice.volume24h / 1_000_000).toFixed(1)}M` },
            { label: '시가총액', value: `$${(livePrice.marketCap / 1_000_000_000).toFixed(2)}B` },
            { label: '24h 변동 (USD)', value: `${livePrice.change24h >= 0 ? '+' : ''}$${livePrice.change24h.toFixed(2)}` },
            { label: '최근 업데이트', value: new Date(livePrice.timestamp).toLocaleTimeString('ko-KR') },
          ].map((item) => (
            <div key={item.label} className="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
              <p className="text-xs text-gray-500">{item.label}</p>
              <p className="mt-1 text-sm font-semibold text-gray-900">{item.value}</p>
            </div>
          ))}
        </div>
      )}

      {/* 매수/매도 모달 */}
      <QuickTradeModal
        open={tradeModal.open}
        onClose={() => setTradeModal((t) => ({ ...t, open: false }))}
        symbol={symbol}
        currentPrice={currentPrice}
        defaultType={tradeModal.type}
      />
    </div>
  )
}

export default AssetDetailPage
