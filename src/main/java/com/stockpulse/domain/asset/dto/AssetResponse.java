package com.stockpulse.domain.asset.dto;

import com.stockpulse.domain.asset.entity.Asset;

public record AssetResponse(
        Long id,
        String symbol,
        String name,
        String assetType,
        String coingeckoId,
        String yahooSymbol,
        String logoUrl,
        boolean enabled
) {
    public static AssetResponse from(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getSymbol(),
                asset.getName(),
                asset.getAssetType().name(),
                asset.getCoingeckoId(),
                asset.getYahooSymbol(),
                asset.getLogoUrl(),
                asset.isEnabled()
        );
    }
}
