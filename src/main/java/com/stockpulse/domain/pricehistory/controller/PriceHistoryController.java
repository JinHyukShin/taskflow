package com.stockpulse.domain.pricehistory.controller;

import com.stockpulse.domain.pricehistory.dto.CandlestickData;
import com.stockpulse.domain.pricehistory.service.PriceHistoryService;
import com.stockpulse.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets/{symbol}")
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    public PriceHistoryController(PriceHistoryService priceHistoryService) {
        this.priceHistoryService = priceHistoryService;
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<CandlestickData>>> getHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1M") String period) {
        List<CandlestickData> history = priceHistoryService.getHistory(symbol, period);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/candles")
    public ResponseEntity<ApiResponse<List<CandlestickData>>> getCandles(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1d") String interval) {
        List<CandlestickData> candles = priceHistoryService.getCandles(symbol, interval);
        return ResponseEntity.ok(ApiResponse.success(candles));
    }
}
