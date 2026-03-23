import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useSTOMPSubscription } from '@/hooks/useSTOMP'
import { useFetch } from '@/hooks/useFetch'
import { Card, MetricCard } from '@/components/ui/Card'
import type {
  PriceData,
  ApiResponse,
  PortfolioSummary,
  WatchlistItem,
  Watchlist,
} from '@/types'

// ─── 실시간 가격 카드 ─────────────────────────────────────────────────────────

function PriceCard({ price }: { price: PriceData }) {
  const isPositive = price.changePercent24h >= 0
  const changeColor = isPositive ? 'text-green-600' : 'text-red-500'
  const bgFlash = isPositive ? 'bg-green-50' : 'bg-red-50'

  return (
    <Link
      to={`/assets/${price.symbol}`}
      className={`block rounded-xl border border-gray-200 bg-white p-4 shadow-sm hover:shadow-md transition-all duration-200 hover:border-blue-300 ${bgFlash}`}
    >
      <div className="flex items-center justify-between mb-2">
        <span className="text-sm font-bold text-gray-900">{price.symbol}</span>
        <span className="text-xs text-gray-400">{price.name}</span>
      </div>
      <p className="text-xl font-bold text-gray-900 tabular-nums">
        ${price.price.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
      </p>
      <p className={`text-sm font-medium mt-1 ${changeColor}`}>
        {isPositive ? '+' : ''}{price.changePercent24h.toFixed(2)}%
        <span className="text-xs ml-1">
          ({isPositive ? '+' : ''}{price.change24h.toFixed(2)})
        </span>
      </p>
    </Link>
  )
}

// ─── DashboardPage ────────────────────────────────────────────────────────────

function DashboardPage() {
  const [priceMap, setPriceMap] = useState<Map<string, PriceData>>(new Map())

  // STOMP: 전체 가격 구독
  const { data: priceMessage } = useSTOMPSubscription<PriceData>('/topic/prices/all')

  useEffect(() => {
    if (!priceMessage) return
    setPriceMap((prev) => {
      const next = new Map(prev)
      next.set(priceMessage.symbol, priceMessage)
      return next
    })
  }, [priceMessage])

  // 포트폴리오 요약 (첫 번째 포트폴리오)
  const { data: portfoliosRes } = useFetch<ApiResponse<{ content: { id: number; name: string }[] }>>('/portfolios')
  const firstPortfolioId = portfoliosRes?.data?.content?.[0]?.id ?? null

  const { data: summaryRes, loading: summaryLoading } = useFetch<ApiResponse<PortfolioSummary>>(
    firstPortfolioId ? `/portfolios/${firstPortfolioId}/summary` : null,
    undefined,
    [firstPortfolioId],
  )

  // 관심종목 (첫 번째 watchlist)
  const { data: watchlistsRes } = useFetch<ApiResponse<{ content: Watchlist[] }>>('/watchlists')
  const firstWatchlistId = watchlistsRes?.data?.content?.[0]?.id ?? null

  const { data: watchlistItemsRes, loading: watchlistLoading } = useFetch<ApiResponse<WatchlistItem[]>>(
    firstWatchlistId ? `/watchlists/${firstWatchlistId}/items` : null,
    undefined,
    [firstWatchlistId],
  )

  const summary = summaryRes?.data
  const watchlistItems = watchlistItemsRes?.data ?? []
  const prices = Array.from(priceMap.values())

  return (
    <div className="space-y-6">
      {/* 포트폴리오 요약 */}
      <section>
        <h2 className="text-base font-semibold text-gray-700 mb-3">포트폴리오 요약</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <MetricCard
            title="총 평가금액"
            value={summary ? `$${summary.totalCurrentValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : '—'}
            loading={summaryLoading}
            valueColor="text-blue-600"
          />
          <MetricCard
            title="총 투자금액"
            value={summary ? `$${summary.totalInvested.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : '—'}
            loading={summaryLoading}
          />
          <MetricCard
            title="총 미실현 손익"
            value={summary ? `$${summary.totalUnrealizedPnl.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : '—'}
            loading={summaryLoading}
            valueColor={summary && summary.totalUnrealizedPnl >= 0 ? 'text-green-600' : 'text-red-500'}
          />
          <MetricCard
            title="수익률"
            value={summary ? `${summary.totalPnlPercent >= 0 ? '+' : ''}${summary.totalPnlPercent.toFixed(2)}%` : '—'}
            loading={summaryLoading}
            valueColor={summary && summary.totalPnlPercent >= 0 ? 'text-green-600' : 'text-red-500'}
          />
        </div>
      </section>

      {/* 실시간 가격 */}
      <section>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-base font-semibold text-gray-700">실시간 가격</h2>
          <div className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse inline-block" />
            <span className="text-xs text-gray-400">Live</span>
          </div>
        </div>
        {prices.length === 0 ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-3">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="rounded-xl border border-gray-200 bg-white p-4 h-24 animate-pulse">
                <div className="h-4 bg-gray-200 rounded w-1/2 mb-2" />
                <div className="h-6 bg-gray-200 rounded w-3/4 mb-1" />
                <div className="h-4 bg-gray-200 rounded w-1/3" />
              </div>
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-3">
            {prices.map((price) => (
              <PriceCard key={price.symbol} price={price} />
            ))}
          </div>
        )}
      </section>

      {/* 관심종목 미니 목록 */}
      <section>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-base font-semibold text-gray-700">관심종목</h2>
          <Link to="/watchlist" className="text-xs text-blue-600 hover:underline">
            전체 보기
          </Link>
        </div>
        <Card padding={false}>
          {watchlistLoading ? (
            <div className="p-4 space-y-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="h-10 bg-gray-100 rounded animate-pulse" />
              ))}
            </div>
          ) : watchlistItems.length === 0 ? (
            <p className="text-center text-sm text-gray-400 py-8">
              관심종목이 없습니다.{' '}
              <Link to="/watchlist" className="text-blue-600 hover:underline">
                추가하기
              </Link>
            </p>
          ) : (
            <ul className="divide-y divide-gray-100">
              {watchlistItems.slice(0, 6).map((item) => {
                const live = priceMap.get(item.symbol)
                const price = live?.price ?? item.currentPrice
                const changePct = live?.changePercent24h ?? item.changePercent24h
                const isPositive = (changePct ?? 0) >= 0
                return (
                  <li key={item.symbol}>
                    <Link
                      to={`/assets/${item.symbol}`}
                      className="flex items-center justify-between px-5 py-3 hover:bg-gray-50 transition-colors"
                    >
                      <div>
                        <span className="text-sm font-semibold text-gray-900">{item.symbol}</span>
                        <span className="text-xs text-gray-400 ml-2">{item.name}</span>
                      </div>
                      <div className="text-right">
                        <p className="text-sm font-medium text-gray-900">
                          {price != null ? `$${price.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : '—'}
                        </p>
                        {changePct != null && (
                          <p className={`text-xs font-medium ${isPositive ? 'text-green-600' : 'text-red-500'}`}>
                            {isPositive ? '+' : ''}{changePct.toFixed(2)}%
                          </p>
                        )}
                      </div>
                    </Link>
                  </li>
                )
              })}
            </ul>
          )}
        </Card>
      </section>
    </div>
  )
}

export default DashboardPage
