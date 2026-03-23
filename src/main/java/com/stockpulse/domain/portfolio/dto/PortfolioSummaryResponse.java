package com.stockpulse.domain.portfolio.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummaryResponse(
        Long portfolioId,
        String name,
        BigDecimal totalInvested,
        BigDecimal totalCurrentValue,
        BigDecimal totalUnrealizedPnl,
        BigDecimal totalPnlPercent,
        List<AssetHolding> assets
) {

    public record AssetHolding(
            String symbol,
            String name,
            BigDecimal quantity,
            BigDecimal avgBuyPrice,
            BigDecimal currentPrice,
            BigDecimal currentValue,
            BigDecimal pnl,
            BigDecimal pnlPercent,
            BigDecimal allocation
    ) {
    }
}
