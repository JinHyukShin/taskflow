import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useFetch, useApiCall } from '@/hooks/useFetch'
import { useSTOMPSubscription } from '@/hooks/useSTOMP'
import { Card } from '@/components/ui/Card'
import { Modal, ConfirmDialog } from '@/components/ui/Modal'
import { AssetTypeBadge } from '@/components/ui/Badge'
import type {
  ApiResponse,
  Watchlist,
  WatchlistItem,
  PriceData,
  PageResponse,
} from '@/types'

// ─── 종목 추가 모달 ───────────────────────────────────────────────────────────

type AddItemModalProps = {
  open: boolean
  onClose: () => void
  watchlistId: number
  onSuccess: () => void
}

function AddItemModal({ open, onClose, watchlistId, onSuccess }: AddItemModalProps) {
  const [symbol, setSymbol] = useState('')

  const { execute, loading, error } = useApiCall<unknown, { symbol: string }>(
    `/watchlists/${watchlistId}/items`,
    'POST',
  )

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const result = await execute({ symbol: symbol.toUpperCase() })
    if (result !== null) {
      onSuccess()
      onClose()
      setSymbol('')
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="종목 추가"
      size="sm"
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
            form="add-item-form"
            disabled={loading}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {loading ? '추가 중...' : '추가'}
          </button>
        </>
      }
    >
      <form id="add-item-form" onSubmit={handleSubmit}>
        <label htmlFor="wl-symbol" className="block text-sm font-medium text-gray-700 mb-2">
          심볼 <span className="text-red-500">*</span>
        </label>
        <input
          id="wl-symbol"
          type="text"
          required
          placeholder="BTC, ETH, AAPL..."
          value={symbol}
          onChange={(e) => setSymbol(e.target.value.toUpperCase())}
          className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        {error && <p className="text-sm text-red-500 mt-2">{error}</p>}
      </form>
    </Modal>
  )
}

// ─── 관심종목 목록 카드 ───────────────────────────────────────────────────────

type WatchlistCardProps = {
  watchlist: Watchlist
  priceMap: Map<string, PriceData>
  onRefresh: () => void
}

function WatchlistCard({ watchlist, priceMap, onRefresh }: WatchlistCardProps) {
  const [addModalOpen, setAddModalOpen] = useState(false)
  const [removeTarget, setRemoveTarget] = useState<string | null>(null)

  const { data: itemsRes, loading, refetch } = useFetch<ApiResponse<WatchlistItem[]>>(
    `/watchlists/${watchlist.id}/items`,
    undefined,
    [watchlist.id],
  )

  const { execute: removeItem, loading: removing } = useApiCall<unknown>(
    removeTarget ? `/watchlists/${watchlist.id}/items/${removeTarget}` : '',
    'DELETE',
  )

  const handleRemove = async () => {
    if (!removeTarget) return
    await removeItem()
    setRemoveTarget(null)
    refetch()
    onRefresh()
  }

  const items = itemsRes?.data ?? []

  return (
    <Card padding={false}>
      <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
        <h3 className="font-semibold text-gray-900">{watchlist.name}</h3>
        <button
          type="button"
          onClick={() => setAddModalOpen(true)}
          className="rounded-lg bg-blue-50 text-blue-600 px-3 py-1.5 text-xs font-medium hover:bg-blue-100 transition-colors"
        >
          + 종목 추가
        </button>
      </div>

      {loading ? (
        <div className="p-4 space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="h-12 animate-pulse rounded bg-gray-100" />
          ))}
        </div>
      ) : items.length === 0 ? (
        <p className="text-center text-sm text-gray-400 py-8">관심종목을 추가하세요.</p>
      ) : (
        <ul className="divide-y divide-gray-100">
          {items.map((item) => {
            const live = priceMap.get(item.symbol)
            const price = live?.price ?? item.currentPrice
            const changePct = live?.changePercent24h ?? item.changePercent24h
            const isPositive = (changePct ?? 0) >= 0

            return (
              <li key={item.symbol} className="flex items-center justify-between px-5 py-3 hover:bg-gray-50 transition-colors">
                <div className="flex items-center gap-3">
                  <div>
                    <Link
                      to={`/assets/${item.symbol}`}
                      className="text-sm font-semibold text-gray-900 hover:text-blue-600 transition-colors"
                    >
                      {item.symbol}
                    </Link>
                    <div className="flex items-center gap-2 mt-0.5">
                      <span className="text-xs text-gray-400">{item.name}</span>
                      <AssetTypeBadge type={item.assetType} />
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-4">
                  <div className="text-right">
                    <p className="text-sm font-medium text-gray-900 tabular-nums">
                      {price != null
                        ? `$${price.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                        : '—'}
                    </p>
                    {changePct != null && (
                      <p className={`text-xs font-medium ${isPositive ? 'text-green-600' : 'text-red-500'}`}>
                        {isPositive ? '+' : ''}{changePct.toFixed(2)}%
                      </p>
                    )}
                  </div>
                  <button
                    type="button"
                    onClick={() => setRemoveTarget(item.symbol)}
                    className="text-gray-300 hover:text-red-500 transition-colors"
                    aria-label={`${item.symbol} 제거`}
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              </li>
            )
          })}
        </ul>
      )}

      {/* 추가 모달 */}
      <AddItemModal
        open={addModalOpen}
        onClose={() => setAddModalOpen(false)}
        watchlistId={watchlist.id}
        onSuccess={refetch}
      />

      {/* 제거 확인 */}
      <ConfirmDialog
        open={removeTarget !== null}
        onClose={() => setRemoveTarget(null)}
        onConfirm={handleRemove}
        title="종목 제거"
        message={removeTarget ? `${removeTarget}을(를) 관심종목에서 제거하시겠습니까?` : ''}
        confirmLabel="제거"
        loading={removing}
      />
    </Card>
  )
}

// ─── WatchlistPage ────────────────────────────────────────────────────────────

function WatchlistPage() {
  const [priceMap, setPriceMap] = useState<Map<string, PriceData>>(new Map())
  const [createModalOpen, setCreateModalOpen] = useState(false)
  const [newListName, setNewListName] = useState('')

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

  // 관심종목 리스트 목록
  const { data: watchlistsRes, loading, refetch } = useFetch<ApiResponse<PageResponse<Watchlist>>>('/watchlists')

  const watchlists = watchlistsRes?.data?.content ?? []

  // 관심종목 리스트 생성
  const { execute: createWatchlist, loading: creating, error: createError } = useApiCall<unknown, { name: string }>(
    '/watchlists',
    'POST',
  )

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const result = await createWatchlist({ name: newListName })
    if (result !== null) {
      setCreateModalOpen(false)
      setNewListName('')
      refetch()
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <h2 className="text-base font-semibold text-gray-700">관심종목</h2>
          <div className="flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse inline-block" />
            <span className="text-xs text-gray-400">실시간</span>
          </div>
        </div>
        <button
          type="button"
          onClick={() => setCreateModalOpen(true)}
          className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 transition-colors"
        >
          + 리스트 생성
        </button>
      </div>

      {loading ? (
        <div className="space-y-4">
          {Array.from({ length: 2 }).map((_, i) => (
            <div key={i} className="rounded-xl border border-gray-200 h-48 animate-pulse bg-gray-100" />
          ))}
        </div>
      ) : watchlists.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500 mb-3">관심종목 리스트가 없습니다.</p>
          <button
            type="button"
            onClick={() => setCreateModalOpen(true)}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 transition-colors"
          >
            첫 번째 리스트 만들기
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {watchlists.map((wl) => (
            <WatchlistCard
              key={wl.id}
              watchlist={wl}
              priceMap={priceMap}
              onRefresh={refetch}
            />
          ))}
        </div>
      )}

      {/* 관심종목 리스트 생성 모달 */}
      <Modal
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        title="관심종목 리스트 생성"
        size="sm"
        footer={
          <>
            <button
              type="button"
              onClick={() => setCreateModalOpen(false)}
              className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
            >
              취소
            </button>
            <button
              type="submit"
              form="create-watchlist-form"
              disabled={creating}
              className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50 transition-colors"
            >
              {creating ? '생성 중...' : '생성'}
            </button>
          </>
        }
      >
        <form id="create-watchlist-form" onSubmit={handleCreateSubmit}>
          <label htmlFor="wl-name" className="block text-sm font-medium text-gray-700 mb-2">
            리스트 이름 <span className="text-red-500">*</span>
          </label>
          <input
            id="wl-name"
            type="text"
            required
            placeholder="예: 암호화폐 관심종목"
            value={newListName}
            onChange={(e) => setNewListName(e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {createError && <p className="text-sm text-red-500 mt-2">{createError}</p>}
        </form>
      </Modal>
    </div>
  )
}

export default WatchlistPage
