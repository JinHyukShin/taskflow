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
import java.util.List;

@Component
public class YahooFinanceClient {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceClient.class);

    private final WebClient webClient;
    private final String baseUrl;

    public YahooFinanceClient(WebClient webClient,
                              @Value("${external.yahoo.base-url}") String baseUrl) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
    }

    public PriceData getPrice(String symbol) {
        JsonNode response = webClient.get()
                .uri(baseUrl + "/v8/finance/chart/{symbol}?interval=1d&range=2d", symbol)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(5))
                .retry(2)
                .block();

        if (response == null) {
            throw new RuntimeException("No response from Yahoo Finance for " + symbol);
        }

        JsonNode result = response.path("chart").path("result").get(0);
        JsonNode meta = result.path("meta");

        double price = meta.path("regularMarketPrice").asDouble(0);
        double previousClose = meta.path("chartPreviousClose").asDouble(0);
        double change24h = price - previousClose;
        double changePercent = previousClose > 0 ? (change24h / previousClose) * 100 : 0;

        return new PriceData(
                symbol,
                meta.path("shortName").asText(symbol),
                price,
                meta.path("currency").asText("USD"),
                change24h,
                changePercent,
                meta.path("regularMarketVolume").asDouble(0),
                0,
                Instant.now()
        );
    }

    public List<CandlestickData> getHistory(String symbol, String range, String interval) {
        JsonNode response = webClient.get()
                .uri(baseUrl + "/v8/finance/chart/{symbol}?range={range}&interval={interval}",
                        symbol, range, interval)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(5))
                .retry(2)
                .block();

        List<CandlestickData> candles = new ArrayList<>();
        if (response == null) {
            return candles;
        }

        JsonNode result = response.path("chart").path("result").get(0);
        JsonNode timestamps = result.path("timestamp");
        JsonNode indicators = result.path("indicators").path("quote").get(0);

        if (timestamps == null || !timestamps.isArray()) {
            return candles;
        }

        for (int i = 0; i < timestamps.size(); i++) {
            double open = indicators.path("open").get(i).asDouble(0);
            double high = indicators.path("high").get(i).asDouble(0);
            double low = indicators.path("low").get(i).asDouble(0);
            double close = indicators.path("close").get(i).asDouble(0);
            double volume = indicators.path("volume").get(i).asDouble(0);

            candles.add(new CandlestickData(
                    timestamps.get(i).asLong(),
                    open, high, low, close, volume
            ));
        }

        return candles;
    }
}
