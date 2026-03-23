import { useState, useEffect } from 'react'
import { useFetch, useApiCall } from '@/hooks/useFetch'
import { useSSE } from '@/hooks/useSSE'
import { Table, type ColumnDef } from '@/components/ui/Table'
import { AlertStatusBadge } from '@/components/ui/Badge'
import { Modal, ConfirmDialog } from '@/components/ui/Modal'
import type {
  ApiResponse,
  PriceAlert,
  CreateAlertRequest,
  AlertCondition,
  AlertNotification,
  PageResponse,
} from '@/types'

// ─── 토스트 알림 ──────────────────────────────────────────────────────────────

type ToastItem = {
  id: string
  notification: AlertNotification
}

function Toast({ item, onDismiss }: { item: ToastItem; onDismiss: (id: string) => void }) {
  useEffect(() => {
    const timer = setTimeout(() => onDismiss(item.id), 6000)
    return () => clearTimeout(timer)
  }, [item.id, onDismiss])

  const { notification: n } = item
  const isAbove = n.condition === 'ABOVE'

  return (
    <div className="flex items-start gap-3 rounded-xl bg-white border border-gray-200 shadow-lg p-4 max-w-sm">
      <div className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center ${isAbove ? 'bg-green-100' : 'bg-red-100'}`}>
        <span className="text-lg">{isAbove ? '↑' : '↓'}</span>
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-semibold text-gray-900">가격 알림 발동!</p>
        <p className="text-xs text-gray-600 mt-0.5">
          {n.assetName} ({n.symbol}) 현재가 ${n.currentPrice.toLocaleString(undefined, { minimumFractionDigits: 2 })}
          {' '}{isAbove ? '이상' : '이하'} 도달
        </p>
        <p className="text-xs text-gray-400 mt-1">목표가: ${n.targetPrice.toLocaleString(undefined, { minimumFractionDigits: 2 })}</p>
      </div>
      <button
        type="button"
        onClick={() => onDismiss(item.id)}
        className="text-gray-400 hover:text-gray-600 flex-shrink-0"
        aria-label="알림 닫기"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
  )
}

// ─── 알림 생성 모달 ───────────────────────────────────────────────────────────

type CreateAlertModalProps = {
  open: boolean
  onClose: () => void
  onSuccess: () => void
}

function CreateAlertModal({ open, onClose, onSuccess }: CreateAlertModalProps) {
  const [form, setForm] = useState<CreateAlertRequest>({
    symbol: '',
    condition: 'ABOVE',
    targetPrice: 0,
    currency: 'USD',
  })

  const { execute, loading, error } = useApiCall<unknown, CreateAlertRequest>('/alerts', 'POST')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const result = await execute(form)
    if (result !== null) {
      onSuccess()
      onClose()
      setForm({ symbol: '', condition: 'ABOVE', targetPrice: 0, currency: 'USD' })
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="가격 알림 생성"
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
            form="create-alert-form"
            disabled={loading}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {loading ? '생성 중...' : '알림 생성'}
          </button>
        </>
      }
    >
      <form id="create-alert-form" onSubmit={handleSubmit} className="space-y-4">
        {/* 심볼 */}
        <div>
          <label htmlFor="alert-symbol" className="block text-sm font-medium text-gray-700 mb-1">
            심볼 <span className="text-red-500">*</span>
          </label>
          <input
            id="alert-symbol"
            type="text"
            required
            placeholder="BTC, ETH, AAPL..."
            value={form.symbol}
            onChange={(e) => setForm((f) => ({ ...f, symbol: e.target.value.toUpperCase() }))}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 조건 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            조건 <span className="text-red-500">*</span>
          </label>
          <div className="flex gap-2">
            {(['ABOVE', 'BELOW'] as AlertCondition[]).map((c) => (
              <button
                key={c}
                type="button"
                onClick={() => setForm((f) => ({ ...f, condition: c }))}
                className={`flex-1 py-2 rounded-lg text-sm font-medium transition-colors ${
                  form.condition === c
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {c === 'ABOVE' ? '이상 (↑)' : '이하 (↓)'}
              </button>
            ))}
          </div>
        </div>

        {/* 목표가 */}
        <div>
          <label htmlFor="alert-price" className="block text-sm font-medium text-gray-700 mb-1">
            목표가 (USD) <span className="text-red-500">*</span>
          </label>
          <input
            id="alert-price"
            type="number"
            required
            min="0"
            step="any"
            placeholder="0.00"
            value={form.targetPrice || ''}
            onChange={(e) => setForm((f) => ({ ...f, targetPrice: parseFloat(e.target.value) || 0 }))}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {form.symbol && form.targetPrice > 0 && (
          <div className="rounded-lg bg-blue-50 border border-blue-100 p-3">
            <p className="text-xs text-blue-700">
              <span className="font-semibold">{form.symbol}</span> 가격이{' '}
              <span className="font-semibold">${form.targetPrice.toLocaleString(undefined, { minimumFractionDigits: 2 })}</span>
              {form.condition === 'ABOVE' ? ' 이상' : ' 이하'}이 되면 알림을 받습니다.
            </p>
          </div>
        )}

        {error && <p className="text-sm text-red-500">{error}</p>}
      </form>
    </Modal>
  )
}

// ─── AlertsPage ───────────────────────────────────────────────────────────────

function AlertsPage() {
  const [createModalOpen, setCreateModalOpen] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<PriceAlert | null>(null)
  const [toasts, setToasts] = useState<ToastItem[]>([])

  // SSE: 실시간 알림 수신
  const { data: alertNotification } = useSSE<AlertNotification>('/api/v1/alerts/stream', 'alert')

  useEffect(() => {
    if (!alertNotification) return
    const toastItem: ToastItem = {
      id: `${alertNotification.alertId}-${Date.now()}`,
      notification: alertNotification,
    }
    setToasts((prev) => [toastItem, ...prev].slice(0, 5))
  }, [alertNotification])

  const handleDismissToast = (id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }

  // 알림 목록
  const { data: alertsRes, loading, refetch } = useFetch<ApiResponse<PageResponse<PriceAlert>>>('/alerts')

  const alerts = alertsRes?.data?.content ?? []

  // 알림 삭제
  const { execute: deleteAlert, loading: deleting } = useApiCall<unknown>(
    deleteTarget ? `/alerts/${deleteTarget.id}` : '',
    'DELETE',
  )

  const handleDelete = async () => {
    if (!deleteTarget) return
    await deleteAlert()
    setDeleteTarget(null)
    refetch()
  }

  const columns: ColumnDef<PriceAlert>[] = [
    {
      key: 'symbol',
      header: '심볼',
      render: (row) => <span className="font-semibold">{row.symbol}</span>,
    },
    { key: 'assetName', header: '자산명' },
    {
      key: 'condition',
      header: '조건',
      render: (row) => (
        <span className={`text-sm font-medium ${row.condition === 'ABOVE' ? 'text-green-600' : 'text-red-500'}`}>
          {row.condition === 'ABOVE' ? '↑ 이상' : '↓ 이하'}
        </span>
      ),
    },
    {
      key: 'targetPrice',
      header: '목표가',
      align: 'right',
      render: (row) => (
        <span className="tabular-nums font-medium">
          ${row.targetPrice.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
        </span>
      ),
    },
    {
      key: 'status',
      header: '상태',
      render: (row) => <AlertStatusBadge status={row.status} />,
    },
    {
      key: 'createdAt',
      header: '생성일',
      render: (row) => new Date(row.createdAt).toLocaleDateString('ko-KR'),
    },
    {
      key: 'triggeredAt',
      header: '발동일',
      render: (row) => row.triggeredAt ? new Date(row.triggeredAt).toLocaleDateString('ko-KR') : '—',
    },
    {
      key: 'action',
      header: '',
      render: (row) => (
        <button
          type="button"
          onClick={() => setDeleteTarget(row)}
          className="text-xs text-red-500 hover:text-red-700 hover:underline"
        >
          삭제
        </button>
      ),
    },
  ]

  return (
    <>
      {/* 토스트 알림 (고정 위치) */}
      <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
        {toasts.map((t) => (
          <Toast key={t.id} item={t} onDismiss={handleDismissToast} />
        ))}
      </div>

      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-semibold text-gray-700">가격 알림</h2>
          <button
            type="button"
            onClick={() => setCreateModalOpen(true)}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 transition-colors"
          >
            + 알림 생성
          </button>
        </div>

        <Table
          columns={columns}
          data={alerts}
          keyExtractor={(row) => row.id}
          loading={loading}
          emptyMessage="등록된 알림이 없습니다."
          caption="가격 알림 목록"
        />

        {/* 알림 생성 모달 */}
        <CreateAlertModal
          open={createModalOpen}
          onClose={() => setCreateModalOpen(false)}
          onSuccess={refetch}
        />

        {/* 삭제 확인 다이얼로그 */}
        <ConfirmDialog
          open={deleteTarget !== null}
          onClose={() => setDeleteTarget(null)}
          onConfirm={handleDelete}
          title="알림 삭제"
          message={deleteTarget ? `${deleteTarget.symbol} 알림을 삭제하시겠습니까?` : ''}
          confirmLabel="삭제"
          loading={deleting}
        />
      </div>
    </>
  )
}

export default AlertsPage
