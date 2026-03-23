package com.stockpulse.infra.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.stockpulse.infra.external.dto.CandlestickData;
import com.stockpulse.infra.external.dto.PriceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CoinGeckoClient {

    private static final Logger log = LoggerFactory.getLogger(CoinGeckoClient.class);

    private final WebClient webClient;
    private final String baseUrl;

    public CoinGeckoClient(WebClient webClient,
                           @Value("${external.coingecko.base-url}") String baseUrl) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
    }

    public Map<String, PriceData> getPrices(List<String> coinIds) {
        if (coinIds == null || coinIds.isEmpty()) {
            return Map.of();
        }

        String ids = String.join(",", coinIds);
        JsonNode response = webClient.get()
                .uri(baseUrl + "/simple/price?ids={ids}&vs_currencies=usd" +
                        "&include_24hr_change=true&include_24hr_vol=true&include_market_cap=true", ids)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(5))
                .retry(2)
                .block();

        Map<String, PriceData> result = new HashMap<>();
        if (response == null) {
            return result;
        }

        var fields = response.fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            String coinId = entry.getKey();
            JsonNode data = entry.getValue();

            double price = data.path("usd").asDouble(0);
            double change24h = data.path("usd_24h_change").asDouble(0);
            double volume24h = data.path("usd_24h_vol").asDouble(0);
            double marketCap = data.path("usd_market_cap").asDouble(0);

            PriceData priceData = new PriceData(
                    coinId,
                    coinId,
                    price,
                    "USD",
                    change24h,
                    price > 0 ? (change24h / price) * 100 : 0,
                    volume24h,
                    marketCap,
                    Instant.now()
            );
            result.put(coinId, priceData);
        }

        return result;
    }

    public List<CandlestickData> getMarketChart(String coinId, int days) {
        JsonNode response = webClient.get()
                .uri(baseUrl + "/coins/{id}/ohlc?vs_currency=usd&days={days}", coinId, days)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(5))
                .retry(2)
                .block();

        List<CandlestickData> candles = new ArrayList<>();
        if (response == null || !response.isArray()) {
            return candles;
        }

        for (JsonNode candle : response) {
            if (candle.isArray() && candle.size() >= 5) {
                candles.add(new CandlestickData(
                        candle.get(0).asLong() / 1000,
                        candle.get(1).asDouble(),
                        candle.get(2).asDouble(),
                        candle.get(3).asDouble(),
                        candle.get(4).asDouble(),
                        0
                ));
            }
        }

        return candles;
    }
}
