package com.stockpulse.infra.external;

import com.stockpulse.domain.asset.entity.AssetType;
import com.stockpulse.global.exception.BusinessException;
import com.stockpulse.global.exception.ErrorCode;
import com.stockpulse.infra.external.dto.PriceData;
import com.stockpulse.infra.redis.PriceCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExternalPriceService {

    private static final Logger log = LoggerFactory.getLogger(ExternalPriceService.class);

    private final CoinGeckoClient coinGeckoClient;
    private final YahooFinanceClient yahooFinanceClient;
    private final PriceCacheService cacheService;

    public ExternalPriceService(CoinGeckoClient coinGeckoClient,
                                YahooFinanceClient yahooFinanceClient,
                                PriceCacheService cacheService) {
        this.coinGeckoClient = coinGeckoClient;
        this.yahooFinanceClient = yahooFinanceClient;
        this.cacheService = cacheService;
    }

    public PriceData getPrice(String symbol, AssetType type) {
        // 1. Redis cache check
        PriceData cached = cacheService.getPrice(symbol);
        if (cached != null) {
            return cached;
        }

        // 2. External API call
        PriceData price;
        try {
            if (type == AssetType.CRYPTO) {
                Map<String, PriceData> prices = coinGeckoClient.getPrices(List.of(symbol));
                price = prices.get(symbol);
                if (price == null) {
                    throw new RuntimeException("No price data returned for " + symbol);
                }
            } else {
                price = yahooFinanceClient.getPrice(symbol);
            }
        } catch (Exception e) {
            log.warn("External API call failed for {}: {}", symbol, e.getMessage());
            // 3. Fallback: stale cache
            PriceData stale = cacheService.getStalePrice(symbol);
            if (stale != null) {
                return stale.withStale(true);
            }
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR,
                    "Price data unavailable for " + symbol);
        }

        // 4. Cache the result
        cacheService.setPrice(symbol, price);
        return price;
    }

    public List<PriceData> getBatchPrices(List<String> coinIds, AssetType type) {
        if (type == AssetType.CRYPTO) {
            try {
                Map<String, PriceData> prices = coinGeckoClient.getPrices(coinIds);
                return new ArrayList<>(prices.values());
            } catch (Exception e) {
                log.warn("Batch price fetch failed: {}", e.getMessage());
                return List.of();
            }
        }
        // For stocks, fetch individually
        List<PriceData> result = new ArrayList<>();
        for (String symbol : coinIds) {
            try {
                result.add(yahooFinanceClient.getPrice(symbol));
            } catch (Exception e) {
                log.warn("Price fetch failed for {}: {}", symbol, e.getMessage());
            }
        }
        return result;
    }
}
