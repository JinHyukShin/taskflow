package com.stockpulse.domain.auth.controller;

import com.stockpulse.domain.auth.dto.LoginRequest;
import com.stockpulse.domain.auth.dto.RefreshRequest;
import com.stockpulse.domain.auth.dto.SignupRequest;
import com.stockpulse.domain.auth.dto.TokenResponse;
import com.stockpulse.domain.auth.dto.UserResponse;
import com.stockpulse.domain.auth.entity.User;
import com.stockpulse.domain.auth.service.AuthService;
import com.stockpulse.global.common.ApiResponse;
import com.stockpulse.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<ApiResponse<TokenResponse>> signup(@Valid @RequestBody SignupRequest request) {
        TokenResponse tokens = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(tokens));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokens = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse tokens = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = authService.findUserById(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
    }
}
