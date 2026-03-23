package com.stockpulse.domain.auth.dto;

import com.stockpulse.domain.auth.entity.User;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String name,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCreatedAt()
        );
    }
}
