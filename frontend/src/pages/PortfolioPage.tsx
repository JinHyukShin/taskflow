import { useState } from 'react'
import { useFetch, useApiCall } from '@/hooks/useFetch'
import { Card, CardHeader, CardBody } from '@/components/ui/Card'
import { Table, type ColumnDef } from '@/components/ui/Table'
import { Modal } from '@/components/ui/Modal'
import { PnlBadge, TradeTypeBadge } from '@/components/ui/Badge'
import { PieChart } from '@/components/charts/PieChart'
import type {
  ApiResponse,
  Portfolio,
  PortfolioSummary,
  PortfolioAsset,
  TradeRequest,
  TradeType,
  PageResponse,
} from '@/types'

// ─── 매수/매도 모달 ───────────────────────────────────────────────────────────

type TradeModalProps = {
  open: boolean
  onClose: () => void
  portfolioId: number
  onSuccess: () => void
  defaultType?: TradeType
  defaultSymbol?: string
}

function TradeModal({ open, onClose, portfolioId, onSuccess, defaultType = 'BUY', defaultSymbol = '' }: TradeModalProps) {
  const [form, setForm] = useState<TradeRequest>({
    symbol: defaultSymbol,
    tradeType: defaultType,
    quantity: 0,
    pricePerUnit: 0,
    currency: 'USD',
  })

  const { execute, loading, error } = useApiCall<unknown, TradeRequest>(
    `/portfolios/${portfolioId}/trades`,
    'POST',
  )

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const result = await execute(form)
    if (result !== null) {
      onSuccess()
      onClose()
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={`${form.tradeType === 'BUY' ? '매수' : '매도'} 등록`}
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
            form="trade-form"
            disabled={loading}
            className={`rounded-lg px-4 py-2 text-sm font-medium text-white transition-colors disabled:opacity-50 ${
              form.tradeType === 'BUY' ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700'
            }`}
          >
            {loading ? '처리 중...' : form.tradeType === 'BUY' ? '매수' : '매도'}
          </button>
        </>
      }
    >
      <form id="trade-form" onSubmit={handleSubmit} className="space-y-4">
        {/* 거래 유형 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">거래 유형</label>
          <div className="flex gap-2">
            {(['BUY', 'SELL'] as TradeType[]).map((t) => (
              <button
                key={t}
                type="button"
                onClick={() => setForm((f) => ({ ...f, tradeType: t }))}
                className={`flex-1 py-2 rounded-lg text-sm font-medium transition-colors ${
                  form.tradeType === t
                    ? t === 'BUY' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {t === 'BUY' ? '매수' : '매도'}
              </button>
            ))}
          </div>
        </div>

        {/* 심볼 */}
        <div>
          <label htmlFor="trade-symbol" className="block text-sm font-medium text-gray-700 mb-1">
            심볼
          </label>
          <input
            id="trade-symbol"
            type="text"
            required
            placeholder="BTC, ETH, AAPL..."
            value={form.symbol}
            onChange={(e) => setForm((f) => ({ ...f, symbol: e.target.value.toUpperCase() }))}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 수량 */}
        <div>
          <label htmlFor="trade-qty" className="block text-sm font-medium text-gray-700 mb-1">
            수량
          </label>
          <input
            id="trade-qty"
            type="number"
            required
            min="0"
            step="any"
            value={form.quantity || ''}
            onChange={(e) => setForm((f) => ({ ...f, quantity: parseFloat(e.target.value) || 0 }))}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 단가 */}
        <div>
          <label htmlFor="trade-price" className="block text-sm font-medium text-gray-700 mb-1">
            단가 (USD)
          </label>
          <input
            id="trade-price"
            type="number"
            required
            min="0"
            step="any"
            value={form.pricePerUnit || ''}
            onChange={(e) => setForm((f) => ({ ...f, pricePerUnit: parseFloat(e.target.value) || 0 }))}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 총 금액 미리보기 */}
        {form.quantity > 0 && form.pricePerUnit > 0 && (
          <div className="rounded-lg bg-gray-50 p-3">
            <p className="text-sm text-gray-600">
              총 금액:{' '}
              <span className="font-semibold text-gray-900">
                ${(form.quantity * form.pricePerUnit).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </span>
            </p>
          </div>
        )}

        {error && (
          <p className="text-sm text-red-500">{error}</p>
        )}
      </form>
    </Modal>
  )
}

// ─── 포트폴리오 생성 모달 ──────────────────────────────────────────────────────

type CreatePortfolioModalProps = {
  open: boolean
  onClose: () => void
  onSuccess: () => void
}

function CreatePortfolioModal({ open, onClose, onSuccess }: CreatePortfolioModalProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  const { execute, loading, error } = useApiCall<unknown, { name: string; description: string }>(
    '/portfolios',
    'POST',
  )

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const result = await execute({ name, description })
    if (result !== null) {
      onSuccess()
      onClose()
      setName('')
      setDescription('')
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="포트폴리오 생성"
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
            form="create-portfolio-form"
            disabled={loading}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {loading ? '생성 중...' : '생성'}
          </button>
        </>
      }
    >
      <form id="create-portfolio-form" onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="portfolio-name" className="block text-sm font-medium text-gray-700 mb-1">
            포트폴리오 이름 <span className="text-red-500">*</span>
          </label>
          <input
            id="portfolio-name"
            type="text"
            required
            placeholder="예: 메인 포트폴리오"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label htmlFor="portfolio-desc" className="block text-sm font-medium text-gray-700 mb-1">
            설명
          </label>
          <textarea
            id="portfolio-desc"
            rows={3}
            placeholder="포트폴리오 설명..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
          />
        </div>
        {error && <p className="text-sm text-red-500">{error}</p>}
      </form>
    </Modal>
  )
}

// ─── 포트폴리오 상세 뷰 ───────────────────────────────────────────────────────

type PortfolioDetailProps = {
  portfolioId: number
}

function PortfolioDetail({ portfolioId }: PortfolioDetailProps) {
  const [tradeModalOpen, setTradeModalOpen] = useState(false)
  const [defaultTradeType, setDefaultTradeType] = useState<TradeType>('BUY')

  const { data: summaryRes, loading, refetch } = useFetch<ApiResponse<PortfolioSummary>>(
    `/portfolios/${portfolioId}/summary`,
    undefined,
    [portfolioId],
  )

  const summary = summaryRes?.data

  const assetColumns: ColumnDef<PortfolioAsset>[] = [
    { key: 'symbol', header: '심볼', render: (row) => <span className="font-semibold">{row.symbol}</span> },
    { key: 'name', header: '이름' },
    {
      key: 'quantity',
      header: '수량',
      align: 'right',
      render: (row) => <span className="tabular-nums">{row.quantity.toLocaleString()}</span>,
    },
    {
      key: 'avgBuyPrice',
      header: '평균단가',
      align: 'right',
      render: (row) => (
        <span className="tabular-nums">
          ${row.avgBuyPrice.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
        </span>
      ),
    },
    {
      key: 'currentPrice',
      header: '현재가',
      align: 'right',
      render: (row) => (
        <span className="tabular-nums font-medium">
          ${row.currentPrice.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
        </span>
      ),
    },
    {
      key: 'pnl',
      header: '손익',
      align: 'right',
      render: (row) => (
        <span className={`tabular-nums font-medium ${row.pnl >= 0 ? 'text-green-600' : 'text-red-500'}`}>
          {row.pnl >= 0 ? '+' : ''}${row.pnl.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
        </span>
      ),
    },
    {
      key: 'pnlPercent',
      header: '수익률',
      align: 'right',
      render: (row) => <PnlBadge pnlPercent={row.pnlPercent} />,
    },
    {
      key: 'allocation',
      header: '비중',
      align: 'right',
      render: (row) => <span className="tabular-nums">{row.allocation.toFixed(1)}%</span>,
    },
  ]

  const pieData = (summary?.assets ?? []).map((a) => ({
    name: `${a.symbol} (${a.allocation.toFixed(1)}%)`,
    value: a.allocation,
  }))

  return (
    <div className="space-y-5">
      {/* 요약 수치 */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { title: '총 평가금액', value: summary ? `$${summary.totalCurrentValue.toLocaleString(undefined, { minimumFractionDigits: 2 })}` : '—', color: 'text-blue-600' },
          { title: '총 투자금액', value: summary ? `$${summary.totalInvested.toLocaleString(undefined, { minimumFractionDigits: 2 })}` : '—', color: 'text-gray-900' },
          {
            title: '미실현 손익',
            value: summary ? `${summary.totalUnrealizedPnl >= 0 ? '+' : ''}$${summary.totalUnrealizedPnl.toLocaleString(undefined, { minimumFractionDigits: 2 })}` : '—',
            color: summary && summary.totalUnrealizedPnl >= 0 ? 'text-green-600' : 'text-red-500',
          },
          {
            title: '수익률',
            value: summary ? `${summary.totalPnlPercent >= 0 ? '+' : ''}${summary.totalPnlPercent.toFixed(2)}%` : '—',
            color: summary && summary.totalPnlPercent >= 0 ? 'text-green-600' : 'text-red-500',
          },
        ].map((m) => (
          <div key={m.title} className="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
            <p className="text-xs font-medium text-gray-500">{m.title}</p>
            <p className={`mt-1 text-xl font-bold tabular-nums ${m.color}`}>{m.value}</p>
          </div>
        ))}
      </div>

      {/* 매수/매도 버튼 */}
      <div className="flex gap-2">
        <button
          type="button"
          onClick={() => { setDefaultTradeType('BUY'); setTradeModalOpen(true) }}
          className="rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 transition-colors"
        >
          + 매수
        </button>
        <button
          type="button"
          onClick={() => { setDefaultTradeType('SELL'); setTradeModalOpen(true) }}
          className="rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 transition-colors"
        >
          - 매도
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
        {/* 보유자산 테이블 */}
        <div className="lg:col-span-2">
          <h3 className="text-sm font-semibold text-gray-700 mb-2">보유 자산</h3>
          <Table
            columns={assetColumns}
            data={summary?.assets ?? []}
            keyExtractor={(row) => row.symbol}
            loading={loading}
            emptyMessage="보유 자산이 없습니다."
          />
        </div>

        {/* 자산 배분 파이 차트 */}
        <div>
          <h3 className="text-sm font-semibold text-gray-700 mb-2">자산 배분</h3>
          <Card>
            {pieData.length > 0 ? (
              <PieChart data={pieData} height={260} />
            ) : (
              <p className="text-center text-sm text-gray-400 py-8">데이터 없음</p>
            )}
          </Card>
        </div>
      </div>

      {/* 거래 모달 */}
      <TradeModal
        open={tradeModalOpen}
        onClose={() => setTradeModalOpen(false)}
        portfolioId={portfolioId}
        onSuccess={refetch}
        defaultType={defaultTradeType}
      />
    </div>
  )
}

// ─── PortfolioPage ────────────────────────────────────────────────────────────

function PortfolioPage() {
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [createModalOpen, setCreateModalOpen] = useState(false)

  const { data: portfoliosRes, loading, refetch } = useFetch<ApiResponse<PageResponse<Portfolio>>>('/portfolios')

  const portfolios = portfoliosRes?.data?.content ?? []

  const portfolioColumns: ColumnDef<Portfolio>[] = [
    {
      key: 'name',
      header: '이름',
      render: (row) => (
        <button
          type="button"
          onClick={() => setSelectedId(row.id)}
          className="font-medium text-blue-600 hover:underline text-left"
        >
          {row.name}
        </button>
      ),
    },
    {
      key: 'description',
      header: '설명',
      render: (row) => <span className="text-gray-500">{row.description ?? '—'}</span>,
    },
    { key: 'currency', header: '통화' },
    {
      key: 'createdAt',
      header: '생성일',
      render: (row) => new Date(row.createdAt).toLocaleDateString('ko-KR'),
    },
    {
      key: 'action',
      header: '',
      render: (row) => (
        <button
          type="button"
          onClick={() => setSelectedId(row.id)}
          className="text-xs text-blue-600 hover:underline"
        >
          선택
        </button>
      ),
    },
  ]

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h2 className="text-base font-semibold text-gray-700">내 포트폴리오</h2>
        <button
          type="button"
          onClick={() => setCreateModalOpen(true)}
          className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 transition-colors"
        >
          + 포트폴리오 생성
        </button>
      </div>

      {/* 포트폴리오 목록 */}
      <Table
        columns={portfolioColumns}
        data={portfolios}
        keyExtractor={(row) => row.id}
        loading={loading}
        emptyMessage="포트폴리오가 없습니다. 새로 생성하세요."
      />

      {/* 선택된 포트폴리오 상세 */}
      {selectedId !== null && (
        <Card padding={false}>
          <CardHeader>
            <div className="flex items-center justify-between">
              <h3 className="font-semibold text-gray-900">
                {portfolios.find((p) => p.id === selectedId)?.name ?? '포트폴리오 상세'}
              </h3>
              <button
                type="button"
                onClick={() => setSelectedId(null)}
                className="text-gray-400 hover:text-gray-600 transition-colors"
                aria-label="닫기"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </CardHeader>
          <CardBody>
            <PortfolioDetail portfolioId={selectedId} />
          </CardBody>
        </Card>
      )}

      {/* 포트폴리오 생성 모달 */}
      <CreatePortfolioModal
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        onSuccess={refetch}
      />
    </div>
  )
}

export default PortfolioPage
