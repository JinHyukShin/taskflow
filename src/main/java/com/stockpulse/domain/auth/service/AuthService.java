package com.stockpulse.domain.auth.service;

import com.stockpulse.domain.auth.dto.LoginRequest;
import com.stockpulse.domain.auth.dto.RefreshRequest;
import com.stockpulse.domain.auth.dto.SignupRequest;
import com.stockpulse.domain.auth.dto.TokenResponse;
import com.stockpulse.domain.auth.entity.RefreshToken;
import com.stockpulse.domain.auth.entity.User;
import com.stockpulse.domain.auth.repository.RefreshTokenRepository;
import com.stockpulse.domain.auth.repository.UserRepository;
import com.stockpulse.global.exception.BusinessException;
import com.stockpulse.global.exception.ErrorCode;
import com.stockpulse.global.security.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtProvider jwtProvider,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public TokenResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_DUPLICATE);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = User.create(request.email(), passwordHash, request.name());
        userRepository.save(user);

        return issueTokens(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        return issueTokens(user);
    }

    public TokenResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED);
        }

        refreshTokenRepository.delete(stored);
        return issueTokens(stored.getUser());
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(
                user.getId().toString(),
                Map.of("email", user.getEmail())
        );
        String refreshToken = jwtProvider.createRefreshToken(user.getId().toString());

        RefreshToken entity = RefreshToken.create(
                user,
                refreshToken,
                Instant.now().plusMillis(jwtProvider.getRefreshTokenExpiry())
        );
        refreshTokenRepository.save(entity);

        return new TokenResponse(accessToken, refreshToken, jwtProvider.getRefreshTokenExpiry() / 1000);
    }
}
