package com.stockpulse.domain.alert.repository;

import com.stockpulse.domain.alert.entity.AlertStatus;
import com.stockpulse.domain.alert.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    List<PriceAlert> findByUserId(Long userId);

    List<PriceAlert> findByStatusAndSymbol(AlertStatus status, String symbol);

    List<PriceAlert> findByStatus(AlertStatus status);
}
