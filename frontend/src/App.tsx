import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from '@/components/layout/Layout'
import DashboardPage from '@/pages/DashboardPage'
import MarketsPage from '@/pages/MarketsPage'
import PortfolioPage from '@/pages/PortfolioPage'
import AssetDetailPage from '@/pages/AssetDetailPage'
import AlertsPage from '@/pages/AlertsPage'
import WatchlistPage from '@/pages/WatchlistPage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<DashboardPage />} />
        <Route path="portfolio" element={<PortfolioPage />} />
        <Route path="watchlist" element={<WatchlistPage />} />
        <Route path="alerts" element={<AlertsPage />} />
        <Route path="markets" element={<MarketsPage />} />
        <Route path="assets/:symbol" element={<AssetDetailPage />} />
        {/* 404 fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}

export default App
