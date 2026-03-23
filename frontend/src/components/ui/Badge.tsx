import React from 'react'
import type { AlertStatus, AssetType, TradeType } from '@/types'

// ─── 자산 타입 뱃지 ───────────────────────────────────────────────────────────

export function AssetTypeBadge({ type }: { type: AssetType }) {
  const styles: Record<AssetType, string> = {
    CRYPTO: 'bg-purple-100 text-purple-800 border border-purple-200',
    STOCK: 'bg-blue-100 text-blue-800 border border-blue-200',
  }
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${styles[type]}`}>
      {type}
    </span>
  )
}

// ─── 알림 상태 뱃지 ───────────────────────────────────────────────────────────

export function AlertStatusBadge({ status }: { status: AlertStatus }) {
  const styles: Record<AlertStatus, string> = {
    ACTIVE: 'bg-green-100 text-green-800 border border-green-200',
    TRIGGERED: 'bg-blue-100 text-blue-800 border border-blue-200',
    DISABLED: 'bg-gray-100 text-gray-600 border border-gray-200',
  }
  const labels: Record<AlertStatus, string> = {
    ACTIVE: '활성',
    TRIGGERED: '발동됨',
    DISABLED: '비활성',
  }
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${styles[status]}`}>
      {labels[status]}
    </span>
  )
}

// ─── 거래 타입 뱃지 ───────────────────────────────────────────────────────────

export function TradeTypeBadge({ type }: { type: TradeType }) {
  const styles: Record<TradeType, string> = {
    BUY: 'bg-green-100 text-green-800 border border-green-200',
    SELL: 'bg-red-100 text-red-800 border border-red-200',
  }
  const labels: Record<TradeType, string> = {
    BUY: '매수',
    SELL: '매도',
  }
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${styles[type]}`}>
      {labels[type]}
    </span>
  )
}

// ─── 손익 뱃지 ───────────────────────────────────────────────────────────────

export function PnlBadge({ pnlPercent }: { pnlPercent: number }) {
  const isPositive = pnlPercent >= 0
  const cls = isPositive
    ? 'bg-green-100 text-green-800 border border-green-200'
    : 'bg-red-100 text-red-800 border border-red-200'
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${cls}`}>
      {isPositive ? '+' : ''}{pnlPercent.toFixed(2)}%
    </span>
  )
}

// ─── 범용 뱃지 ───────────────────────────────────────────────────────────────

type BadgeVariant = 'green' | 'red' | 'yellow' | 'blue' | 'gray' | 'orange' | 'purple'

type BadgeProps = {
  variant?: BadgeVariant
  children: React.ReactNode
  className?: string
}

const variantStyles: Record<BadgeVariant, string> = {
  green: 'bg-green-100 text-green-800 border border-green-200',
  red: 'bg-red-100 text-red-800 border border-red-200',
  yellow: 'bg-yellow-100 text-yellow-800 border border-yellow-200',
  blue: 'bg-blue-100 text-blue-800 border border-blue-200',
  gray: 'bg-gray-100 text-gray-600 border border-gray-200',
  orange: 'bg-orange-100 text-orange-700 border border-orange-200',
  purple: 'bg-purple-100 text-purple-800 border border-purple-200',
}

export function Badge({ variant = 'gray', children, className = '' }: BadgeProps) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${variantStyles[variant]} ${className}`}>
      {children}
    </span>
  )
}
