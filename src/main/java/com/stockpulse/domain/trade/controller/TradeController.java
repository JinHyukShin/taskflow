package com.stockpulse.domain.trade.controller;

import com.stockpulse.domain.trade.dto.TradeRequest;
import com.stockpulse.domain.trade.dto.TradeResponse;
import com.stockpulse.domain.trade.entity.Trade;
import com.stockpulse.domain.trade.service.TradeService;
import com.stockpulse.global.common.ApiResponse;
import com.stockpulse.global.common.PageResponse;
import com.stockpulse.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portfolios/{portfolioId}/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TradeResponse>> executeTrade(
            @PathVariable Long portfolioId,
            @Valid @RequestBody TradeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Trade trade = tradeService.executeTrade(portfolioId, request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(TradeResponse.from(trade)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TradeResponse>>> getTrades(
            @PathVariable Long portfolioId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Trade> trades = tradeService.findTrades(portfolioId, userDetails.getUserId(), pageable);
        Page<TradeResponse> responsePage = trades.map(TradeResponse::from);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }
}
