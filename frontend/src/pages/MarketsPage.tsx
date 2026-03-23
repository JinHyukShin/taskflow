import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useSTOMPSubscription } from '@/hooks/useSTOMP'
import { useFetch } from '@/hooks/useFetch'
import { Table, type ColumnDef } from '@/components/ui/Table'
import { AssetTypeBadge } from '@/components/ui/Badge'
import type { ApiResponse, AssetWithPrice, PriceData, AssetType } from '@/types'

type TabType = 'ALL' | AssetType

function MarketsPage() {
  const [activeTab, setActiveTab] = useState<TabType>('ALL')
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

  // 자산 목록
  const { data: assetsRes, loading } = useFetch<ApiResponse<{ content: AssetWithPrice[] }>>(
    activeTab === 'ALL' ? '/assets' : `/assets?type=${activeTab}`,
    undefined,
    [activeTab],
  )

  const assets = assetsRes?.data?.content ?? []

  // 실시간 가격으로 자산 데이터 병합
  const mergedAssets = assets.map((asset) => {
    const live = priceMap.get(asset.symbol)
    if (live) {
      return {
        ...asset,
        currentPrice: live.price,
        change24h: live.change24h,
        changePercent24h: live.changePercent24h,
        volume24h: live.volume24h,
      }
    }
    return asset
  })

  const columns: ColumnDef<AssetWithPrice>[] = [
    {
      key: 'symbol',
      header: '심볼',
      render: (row) => (
        <Link
          to={`/assets/${row.symbol}`}
          className="font-semibold text-blue-600 hover:underline"
        >
          {row.symbol}
        </Link>
      ),
    },
    {
      key: 'name',
      header: '이름',
      render: (row) => <span className="text-gray-700">{row.name}</span>,
    },
    {
      key: 'assetType',
      header: '유형',
      render: (row) => <AssetTypeBadge type={row.assetType} />,
    },
    {
      key: 'currentPrice',
      header: '현재가',
      align: 'right',
      render: (row) => (
        <span className="font-medium tabular-nums">
          {row.currentPrice != null
            ? `$${row.currentPrice.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
            : '—'}
        </span>
      ),
    },
    {
      key: 'changePercent24h',
      header: '24h 변동',
      align: 'right',
      render: (row) => {
        const pct = row.changePercent24h
        if (pct == null) return <span className="text-gray-400">—</span>
        const isPos = pct >= 0
        return (
          <span className={`font-medium tabular-nums ${isPos ? 'text-green-600' : 'text-red-500'}`}>
            {isPos ? '+' : ''}{pct.toFixed(2)}%
          </span>
        )
      },
    },
    {
      key: 'volume24h',
      header: '거래량 (24h)',
      align: 'right',
      render: (row) => (
        <span className="tabular-nums text-gray-600">
          {row.volume24h != null
            ? `$${(row.volume24h / 1_000_000).toFixed(1)}M`
            : '—'}
        </span>
      ),
    },
    {
      key: 'action',
      header: '',
      render: (row) => (
        <Link
          to={`/assets/${row.symbol}`}
          className="text-xs text-blue-600 hover:underline"
        >
          상세 보기
        </Link>
      ),
    },
  ]

  return (
    <div className="space-y-4">
      {/* 탭 필터 */}
      <div className="flex items-center gap-2">
        {(['ALL', 'CRYPTO', 'STOCK'] as TabType[]).map((tab) => (
          <button
            key={tab}
            type="button"
            onClick={() => setActiveTab(tab)}
            className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${
              activeTab === tab
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {tab === 'ALL' ? '전체' : tab === 'CRYPTO' ? '암호화폐' : '주식'}
          </button>
        ))}

        <div className="ml-auto flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse inline-block" />
          <span className="text-xs text-gray-400">실시간 업데이트</span>
        </div>
      </div>

      {/* 자산 목록 테이블 */}
      <Table
        columns={columns}
        data={mergedAssets}
        keyExtractor={(row) => row.symbol}
        loading={loading}
        emptyMessage="자산 데이터가 없습니다."
        caption="자산 목록"
      />
    </div>
  )
}

export default MarketsPage
