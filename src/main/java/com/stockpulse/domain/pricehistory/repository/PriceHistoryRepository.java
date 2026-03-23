package com.stockpulse.domain.pricehistory.repository;

import com.stockpulse.domain.pricehistory.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findBySymbolAndDateBetweenOrderByDateAsc(
            String symbol, LocalDate startDate, LocalDate endDate);

    List<PriceHistory> findBySymbolOrderByDateDesc(String symbol);

    Optional<PriceHistory> findBySymbolAndDate(String symbol, LocalDate date);
}
