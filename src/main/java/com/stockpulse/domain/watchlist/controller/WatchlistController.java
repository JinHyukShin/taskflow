package com.stockpulse.domain.watchlist.controller;

import com.stockpulse.domain.watchlist.dto.WatchlistItemRequest;
import com.stockpulse.domain.watchlist.dto.WatchlistRequest;
import com.stockpulse.domain.watchlist.dto.WatchlistResponse;
import com.stockpulse.domain.watchlist.entity.Watchlist;
import com.stockpulse.domain.watchlist.service.WatchlistService;
import com.stockpulse.global.common.ApiResponse;
import com.stockpulse.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/watchlists")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WatchlistResponse>> create(
            @RequestBody WatchlistRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Watchlist watchlist = watchlistService.create(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(WatchlistResponse.from(watchlist)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WatchlistResponse>>> findAll(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<WatchlistResponse> watchlists = watchlistService.findAll(userDetails.getUserId()).stream()
                .map(WatchlistResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(watchlists));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<WatchlistResponse>> addItem(
            @PathVariable Long id,
            @Valid @RequestBody WatchlistItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Watchlist watchlist = watchlistService.addItem(id, request.symbol(), userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(WatchlistResponse.from(watchlist)));
    }

    @DeleteMapping("/{id}/items/{symbol}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long id,
            @PathVariable String symbol,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        watchlistService.removeItem(id, symbol, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
