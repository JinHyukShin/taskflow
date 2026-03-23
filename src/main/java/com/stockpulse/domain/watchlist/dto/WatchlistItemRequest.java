package com.stockpulse.domain.watchlist.dto;

import jakarta.validation.constraints.NotBlank;

public record WatchlistItemRequest(
        @NotBlank String symbol
) {
}
