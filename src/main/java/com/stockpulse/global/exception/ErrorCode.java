package com.stockpulse.global.exception;

public enum ErrorCode {

    // --- Common ---
    INVALID_INPUT(400, "INVALID_INPUT", "Invalid input"),
    UNAUTHORIZED(401, "UNAUTHORIZED", "Unauthorized"),
    FORBIDDEN(403, "FORBIDDEN", "Access denied"),
    NOT_FOUND(404, "NOT_FOUND", "Resource not found"),
    DUPLICATE(409, "DUPLICATE", "Resource already exists"),
    INTERNAL_ERROR(500, "INTERNAL_ERROR", "Internal server error"),

    // --- Auth ---
    AUTH_EMAIL_DUPLICATE(409, "AUTH_EMAIL_DUPLICATE", "Email already registered"),
    AUTH_INVALID_CREDENTIALS(401, "AUTH_INVALID_CREDENTIALS", "Invalid email or password"),
    AUTH_TOKEN_EXPIRED(401, "AUTH_TOKEN_EXPIRED", "Token has expired"),
    AUTH_REFRESH_TOKEN_NOT_FOUND(404, "AUTH_REFRESH_TOKEN_NOT_FOUND", "Refresh token not found"),

    // --- Asset ---
    ASSET_NOT_FOUND(404, "ASSET_NOT_FOUND", "Asset not found"),

    // --- Portfolio ---
    PORTFOLIO_NOT_FOUND(404, "PORTFOLIO_NOT_FOUND", "Portfolio not found"),
    PORTFOLIO_ACCESS_DENIED(403, "PORTFOLIO_ACCESS_DENIED", "Portfolio access denied"),

    // --- Trade ---
    TRADE_INSUFFICIENT_QUANTITY(400, "TRADE_INSUFFICIENT_QUANTITY", "Insufficient quantity for sell"),

    // --- Alert ---
    ALERT_NOT_FOUND(404, "ALERT_NOT_FOUND", "Price alert not found"),

    // --- Watchlist ---
    WATCHLIST_NOT_FOUND(404, "WATCHLIST_NOT_FOUND", "Watchlist not found"),
    WATCHLIST_ITEM_DUPLICATE(409, "WATCHLIST_ITEM_DUPLICATE", "Item already in watchlist"),

    // --- External API ---
    EXTERNAL_API_ERROR(502, "EXTERNAL_API_ERROR", "External API is temporarily unavailable"),
    RATE_LIMIT_EXCEEDED(429, "RATE_LIMIT_EXCEEDED", "Rate limit exceeded");

    private final int httpStatus;
    private final String code;
    private final String message;

    ErrorCode(int httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
