package com.stockpulse.domain.watchlist.dto;

import com.stockpulse.domain.watchlist.entity.Watchlist;
import com.stockpulse.domain.watchlist.entity.WatchlistItem;

import java.time.Instant;
import java.util.List;

public record WatchlistResponse(
        Long id,
        String name,
        List<WatchlistItemResponse> items,
        Instant createdAt
) {
    public static WatchlistResponse from(Watchlist watchlist) {
        List<WatchlistItemResponse> items = watchlist.getItems().stream()
                .map(WatchlistItemResponse::from)
                .toList();
        return new WatchlistResponse(
                watchlist.getId(),
                watchlist.getName(),
                items,
                watchlist.getCreatedAt()
        );
    }

    public record WatchlistItemResponse(
            Long id,
            String symbol,
            Instant addedAt
    ) {
        public static WatchlistItemResponse from(WatchlistItem item) {
            return new WatchlistItemResponse(
                    item.getId(),
                    item.getSymbol(),
                    item.getAddedAt()
            );
        }
    }
}
