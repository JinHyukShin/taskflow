package com.stockpulse.infra.websocket;

import com.stockpulse.domain.alert.service.AlertService;
import com.stockpulse.domain.asset.entity.Asset;
import com.stockpulse.domain.asset.entity.AssetType;
import com.stockpulse.domain.asset.repository.AssetRepository;
import com.stockpulse.infra.external.ExternalPriceService;
import com.stockpulse.infra.external.dto.PriceData;
import com.stockpulse.infra.redis.PriceCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PriceStreamScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceStreamScheduler.class);

    private final ExternalPriceService priceService;
    private final PriceCacheService cacheService;
    private final StompPricePublisher publisher;
    private final AlertService alertService;
    private final AssetRepository assetRepository;

    public PriceStreamScheduler(ExternalPriceService priceService,
                                PriceCacheService cacheService,
                                StompPricePublisher publisher,
                                AlertService alertService,
                                AssetRepository assetRepository) {
        this.priceService = priceService;
        this.cacheService = cacheService;
        this.publisher = publisher;
        this.alertService = alertService;
        this.assetRepository = assetRepository;
    }

    @Scheduled(fixedRate = 5000)
    public void streamPrices() {
        try {
            List<Asset> activeAssets = assetRepository.findByEnabledTrue();
            if (activeAssets.isEmpty()) {
                return;
            }

            Map<AssetType, List<Asset>> byType = activeAssets.stream()
                    .collect(Collectors.groupingBy(Asset::getAssetType));

            List<PriceData> allPrices = new ArrayList<>();

            // Crypto: CoinGecko batch query
            if (byType.containsKey(AssetType.CRYPTO)) {
                List<String> cryptoIds = byType.get(AssetType.CRYPTO).stream()
                        .map(Asset::getCoingeckoId)
                        .filter(id -> id != null && !id.isEmpty())
                        .toList();
                if (!cryptoIds.isEmpty()) {
                    allPrices.addAll(priceService.getBatchPrices(cryptoIds, AssetType.CRYPTO));
                }
            }

            // Stocks: Yahoo Finance individual queries
            if (byType.containsKey(AssetType.STOCK)) {
                for (Asset stock : byType.get(AssetType.STOCK)) {
                    try {
                        String symbol = stock.getYahooSymbol() != null
                                ? stock.getYahooSymbol() : stock.getSymbol();
                        allPrices.add(priceService.getPrice(symbol, AssetType.STOCK));
                    } catch (Exception e) {
                        log.debug("Failed to fetch price for stock {}: {}", stock.getSymbol(), e.getMessage());
                    }
                }
            }

            // Cache all prices
            allPrices.forEach(p -> cacheService.setPrice(p.symbol(), p));

            // STOMP broadcast
            if (!allPrices.isEmpty()) {
                publisher.publishAll(allPrices);
                allPrices.forEach(publisher::publish);
            }

            // Check price alerts
            alertService.checkAlerts(allPrices);

        } catch (Exception e) {
            log.error("Price streaming failed: {}", e.getMessage());
        }
    }
}
