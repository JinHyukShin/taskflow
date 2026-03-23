package com.stockpulse.domain.asset.controller;

import com.stockpulse.domain.asset.dto.AssetPriceResponse;
import com.stockpulse.domain.asset.dto.AssetResponse;
import com.stockpulse.domain.asset.entity.AssetType;
import com.stockpulse.domain.asset.service.AssetService;
import com.stockpulse.global.common.ApiResponse;
import com.stockpulse.infra.external.dto.PriceData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AssetResponse>>> findAll(
            @RequestParam(required = false) AssetType type) {
        List<AssetResponse> assets = assetService.findAll(type).stream()
                .map(AssetResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(assets));
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<AssetResponse>> findBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(ApiResponse.success(
                AssetResponse.from(assetService.findBySymbol(symbol))));
    }

    @GetMapping("/{symbol}/price")
    public ResponseEntity<ApiResponse<AssetPriceResponse>> getPrice(@PathVariable String symbol) {
        PriceData price = assetService.getCurrentPrice(symbol);
        return ResponseEntity.ok(ApiResponse.success(AssetPriceResponse.from(price)));
    }

    @GetMapping("/prices")
    public ResponseEntity<ApiResponse<Map<String, AssetPriceResponse>>> getPrices(
            @RequestParam String symbols) {
        List<String> symbolList = Arrays.asList(symbols.split(","));
        Map<String, PriceData> prices = assetService.getPrices(symbolList);
        Map<String, AssetPriceResponse> response = prices.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> AssetPriceResponse.from(e.getValue())));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
