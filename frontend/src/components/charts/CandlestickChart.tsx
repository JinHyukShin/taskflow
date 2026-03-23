import { useEffect, useRef } from 'react'
import {
  createChart,
  type IChartApi,
  type ISeriesApi,
  ColorType,
  CrosshairMode,
} from 'lightweight-charts'
import type { CandlestickData } from '@/types'

type CandlestickChartProps = {
  data: CandlestickData[]
  height?: number
  className?: string
}

export function CandlestickChart({ data, height = 400, className = '' }: CandlestickChartProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const chartRef = useRef<IChartApi | null>(null)
  const seriesRef = useRef<ISeriesApi<'Candlestick'> | null>(null)

  // 차트 초기화
  useEffect(() => {
    if (!containerRef.current) return

    const chart = createChart(containerRef.current, {
      layout: {
        background: { type: ColorType.Solid, color: '#ffffff' },
        textColor: '#6b7280',
      },
      grid: {
        vertLines: { color: '#f3f4f6' },
        horzLines: { color: '#f3f4f6' },
      },
      crosshair: {
        mode: CrosshairMode.Normal,
      },
      rightPriceScale: {
        borderColor: '#e5e7eb',
      },
      timeScale: {
        borderColor: '#e5e7eb',
        timeVisible: true,
        secondsVisible: false,
      },
      width: containerRef.current.clientWidth,
      height,
    })

    const candlestickSeries = chart.addCandlestickSeries({
      upColor: '#22c55e',
      downColor: '#ef4444',
      borderUpColor: '#22c55e',
      borderDownColor: '#ef4444',
      wickUpColor: '#22c55e',
      wickDownColor: '#ef4444',
    })

    chartRef.current = chart
    seriesRef.current = candlestickSeries

    // 리사이즈 핸들러
    const handleResize = () => {
      if (containerRef.current) {
        chart.applyOptions({ width: containerRef.current.clientWidth })
      }
    }
    window.addEventListener('resize', handleResize)

    return () => {
      window.removeEventListener('resize', handleResize)
      chart.remove()
      chartRef.current = null
      seriesRef.current = null
    }
  }, [height])

  // 데이터 업데이트
  useEffect(() => {
    if (!seriesRef.current || data.length === 0) return
    // lightweight-charts는 time을 number(Unix seconds) 또는 string('YYYY-MM-DD')로 받음
    const chartData = data.map((d) => ({
      time: d.time as unknown as number,
      open: d.open,
      high: d.high,
      low: d.low,
      close: d.close,
    }))
    seriesRef.current.setData(chartData)
    chartRef.current?.timeScale().fitContent()
  }, [data])

  return (
    <div ref={containerRef} className={`w-full ${className}`} style={{ height }} />
  )
}
