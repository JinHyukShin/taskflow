package com.stockpulse.infra.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockpulse.infra.external.dto.PriceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PriceCacheService {

    private static final Logger log = LoggerFactory.getLogger(PriceCacheService.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration PRICE_TTL = Duration.ofSeconds(15);
    private static final Duration STALE_TTL = Duration.ofMinutes(5);
    private static final String PRICE_KEY_PREFIX = "price:";
    private static final String STALE_KEY_PREFIX = "price:stale:";

    public PriceCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void setPrice(String symbol, PriceData price) {
        try {
            String json = objectMapper.writeValueAsString(price);
            redisTemplate.opsForValue().set(PRICE_KEY_PREFIX + symbol, json, PRICE_TTL);
            redisTemplate.opsForValue().set(STALE_KEY_PREFIX + symbol, json, STALE_TTL);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize price data for {}: {}", symbol, e.getMessage());
        }
    }

    public PriceData getPrice(String symbol) {
        return deserialize(redisTemplate.opsForValue().get(PRICE_KEY_PREFIX + symbol));
    }

    public PriceData getStalePrice(String symbol) {
        return deserialize(redisTemplate.opsForValue().get(STALE_KEY_PREFIX + symbol));
    }

    public Map<String, PriceData> getPrices(List<String> symbols) {
        List<String> keys = symbols.stream().map(s -> PRICE_KEY_PREFIX + s).toList();
        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        Map<String, PriceData> result = new HashMap<>();
        if (values == null) {
            return result;
        }

        for (int i = 0; i < symbols.size(); i++) {
            PriceData data = deserialize(values.get(i));
            if (data != null) {
                result.put(symbols.get(i), data);
            }
        }
        return result;
    }

    private PriceData deserialize(String json) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, PriceData.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize price data: {}", e.getMessage());
            return null;
        }
    }
}
