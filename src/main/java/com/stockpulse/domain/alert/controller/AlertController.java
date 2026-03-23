package com.stockpulse.domain.alert.controller;

import com.stockpulse.domain.alert.dto.AlertRequest;
import com.stockpulse.domain.alert.dto.AlertResponse;
import com.stockpulse.domain.alert.entity.PriceAlert;
import com.stockpulse.domain.alert.service.AlertService;
import com.stockpulse.global.common.ApiResponse;
import com.stockpulse.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlertResponse>> create(
            @Valid @RequestBody AlertRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PriceAlert alert = alertService.create(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AlertResponse.from(alert)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertResponse>>> findAll(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AlertResponse> alerts = alertService.findAll(userDetails.getUserId()).stream()
                .map(AlertResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AlertRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PriceAlert alert = alertService.update(id, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(AlertResponse.from(alert)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        alertService.delete(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return alertService.subscribe(userDetails.getUserId());
    }
}
