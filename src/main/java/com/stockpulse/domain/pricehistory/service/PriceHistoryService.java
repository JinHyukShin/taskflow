package com.stockpulse.domain.pricehistory.service;

import com.stockpulse.domain.asset.entity.Asset;
import com.stockpulse.domain.asset.entity.AssetType;
import com.stockpulse.domain.asset.repository.AssetRepository;
import com.stockpulse.domain.pricehistory.dto.CandlestickData;
import com.stockpulse.domain.pricehistory.entity.PriceHistory;
import com.stockpulse.domain.pricehistory.repository.PriceHistoryRepository;
import com.stockpulse.infra.external.CoinGeckoClient;
import com.stockpulse.infra.external.YahooFinanceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PriceHistoryService {

    private static final Logger log = LoggerFactory.getLogger(PriceHistoryService.class);

    private final PriceHistoryRepository historyRepository;
    private final AssetRepository assetRepository;
    private final CoinGeckoClient coinGeckoClient;
    private final YahooFinanceClient yahooFinanceClient;

    public PriceHistoryService(PriceHistoryRepository historyRepository,
                               AssetRepository assetRepository,
                               CoinGeckoClient coinGeckoClient,
                               YahooFinanceClient yahooFinanceClient) {
        this.historyRepository = historyRepository;
        this.assetRepository = assetRepository;
        this.coinGeckoClient = coinGeckoClient;
        this.yahooFinanceClient = yahooFinanceClient;
    }

    @Transactional(readOnly = true)
    public List<CandlestickData> getHistory(String symbol, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(period);

        List<PriceHistory> histories = historyRepository
                .findBySymbolAndDateBetweenOrderByDateAsc(symbol, startDate, endDate);

        return histories.stream()
                .map(CandlestickData::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CandlestickData> getCandles(String symbol, String interval) {
        // For daily interval, return from DB
        // For other intervals, we could aggregate, but for now return daily data
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3);

        List<PriceHistory> histories = historyRepository
                .findBySymbolAndDateBetweenOrderByDateAsc(symbol, startDate, endDate);

        return histories.stream()
                .map(CandlestickData::from)
                .toList();
    }

    @Scheduled(cron = "0 5 0 * * *")
    public void collectDailyHistory() {
        log.info("Starting daily price history collection");
        List<Asset> assets = assetRepository.findByEnabledTrue();
        LocalDate yesterday = LocalDate.now().minusDays(1);

        for (Asset asset : assets) {
            try {
                if (historyRepository.findBySymbolAndDate(asset.getSymbol(), yesterday).isPresent()) {
                    continue; // already collected
                }

                List<com.stockpulse.infra.external.dto.CandlestickData> candles;
                if (asset.getAssetType() == AssetType.CRYPTO && asset.getCoingeckoId() != null) {
                    candles = coinGeckoClient.getMarketChart(asset.getCoingeckoId(), 2);
                } else if (asset.getYahooSymbol() != null) {
                    candles = yahooFinanceClient.getHistory(asset.getYahooSymbol(), "5d", "1d");
                } else {
                    continue;
                }

                if (!candles.isEmpty()) {
                    var lastCandle = candles.getLast();
                    PriceHistory history = PriceHistory.create(
                            asset.getSymbol(),
                            yesterday,
                            BigDecimal.valueOf(lastCandle.open()),
                            BigDecimal.valueOf(lastCandle.high()),
                            BigDecimal.valueOf(lastCandle.low()),
                            BigDecimal.valueOf(lastCandle.close()),
                            BigDecimal.valueOf(lastCandle.volume()),
                            "USD"
                    );
                    historyRepository.save(history);
                }
            } catch (Exception e) {
                log.warn("Failed to collect history for {}: {}", asset.getSymbol(), e.getMessage());
            }
        }
        log.info("Daily price history collection completed");
    }

    private LocalDate calculateStartDate(String period) {
        return switch (period != null ? period.toUpperCase() : "1M") {
            case "1D" -> LocalDate.now().minusDays(1);
            case "1W" -> LocalDate.now().minusWeeks(1);
            case "1M" -> LocalDate.now().minusMonths(1);
            case "3M" -> LocalDate.now().minusMonths(3);
            case "1Y" -> LocalDate.now().minusYears(1);
            default -> LocalDate.now().minusMonths(1);
        };
    }
}
