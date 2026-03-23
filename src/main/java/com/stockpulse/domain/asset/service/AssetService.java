package com.stockpulse.domain.asset.service;

import com.stockpulse.domain.asset.entity.Asset;
import com.stockpulse.domain.asset.entity.AssetType;
import com.stockpulse.domain.asset.repository.AssetRepository;
import com.stockpulse.global.exception.BusinessException;
import com.stockpulse.global.exception.ErrorCode;
import com.stockpulse.infra.external.ExternalPriceService;
import com.stockpulse.infra.external.dto.PriceData;
import com.stockpulse.infra.redis.PriceCacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;
    private final ExternalPriceService externalPriceService;
    private final PriceCacheService priceCacheService;

    public AssetService(AssetRepository assetRepository,
                        ExternalPriceService externalPriceService,
                        PriceCacheService priceCacheService) {
        this.assetRepository = assetRepository;
        this.externalPriceService = externalPriceService;
        this.priceCacheService = priceCacheService;
    }

    public List<Asset> findAll(AssetType type) {
        if (type != null) {
            return assetRepository.findByAssetTypeAndEnabledTrue(type);
        }
        return assetRepository.findByEnabledTrue();
    }

    public Asset findBySymbol(String symbol) {
        return assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSET_NOT_FOUND));
    }

    public PriceData getCurrentPrice(String symbol) {
        Asset asset = findBySymbol(symbol);
        String lookupSymbol = asset.getAssetType() == AssetType.CRYPTO
                ? asset.getCoingeckoId() : asset.getYahooSymbol();
        if (lookupSymbol == null) {
            lookupSymbol = symbol;
        }
        return externalPriceService.getPrice(lookupSymbol, asset.getAssetType());
    }

    public Map<String, PriceData> getPrices(List<String> symbols) {
        return priceCacheService.getPrices(symbols);
    }
}
