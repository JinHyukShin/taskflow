package com.stockpulse.domain.portfolio.controller;

import com.stockpulse.domain.portfolio.dto.PortfolioRequest;
import com.stockpulse.domain.portfolio.dto.PortfolioResponse;
import com.stockpulse.domain.portfolio.dto.PortfolioSummaryResponse;
import com.stockpulse.domain.portfolio.entity.Portfolio;
import com.stockpulse.domain.portfolio.service.PortfolioService;
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
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PortfolioResponse>> create(
            @Valid @RequestBody PortfolioRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Portfolio portfolio = portfolioService.create(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(PortfolioResponse.from(portfolio)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PortfolioResponse>>> findAll(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<PortfolioResponse> portfolios = portfolioService.findAll(userDetails.getUserId()).stream()
                .map(PortfolioResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(portfolios));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PortfolioResponse>> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Portfolio portfolio = portfolioService.findById(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(PortfolioResponse.from(portfolio)));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getSummary(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PortfolioSummaryResponse summary = portfolioService.getSummary(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        portfolioService.delete(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
