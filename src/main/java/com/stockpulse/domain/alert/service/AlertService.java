package com.stockpulse.domain.alert.service;

import com.stockpulse.domain.alert.dto.AlertNotification;
import com.stockpulse.domain.alert.dto.AlertRequest;
import com.stockpulse.domain.alert.entity.AlertCondition;
import com.stockpulse.domain.alert.entity.AlertStatus;
import com.stockpulse.domain.alert.entity.PriceAlert;
import com.stockpulse.domain.alert.repository.PriceAlertRepository;
import com.stockpulse.domain.auth.entity.User;
import com.stockpulse.domain.auth.repository.UserRepository;
import com.stockpulse.global.exception.BusinessException;
import com.stockpulse.global.exception.ErrorCode;
import com.stockpulse.infra.external.dto.PriceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final PriceAlertRepository alertRepository;
    private final UserRepository userRepository;
    private final SseEmitterManager sseEmitterManager;

    public AlertService(PriceAlertRepository alertRepository,
                        UserRepository userRepository,
                        SseEmitterManager sseEmitterManager) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.sseEmitterManager = sseEmitterManager;
    }

    public PriceAlert create(AlertRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        AlertCondition condition = AlertCondition.valueOf(request.condition().toUpperCase());
        PriceAlert alert = PriceAlert.create(
                user,
                request.symbol(),
                condition,
                request.targetPrice(),
                request.currency()
        );
        return alertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public List<PriceAlert> findAll(Long userId) {
        return alertRepository.findByUserId(userId);
    }

    public PriceAlert update(Long alertId, AlertRequest request, Long userId) {
        PriceAlert alert = findAlertForUser(alertId, userId);
        AlertCondition condition = AlertCondition.valueOf(request.condition().toUpperCase());
        alert.updateTarget(condition, request.targetPrice());
        alert.activate();
        return alertRepository.save(alert);
    }

    public void delete(Long alertId, Long userId) {
        PriceAlert alert = findAlertForUser(alertId, userId);
        alertRepository.delete(alert);
    }

    public void checkAlerts(List<PriceData> prices) {
        if (prices == null || prices.isEmpty()) {
            return;
        }

        Map<String, BigDecimal> priceMap = new HashMap<>();
        for (PriceData p : prices) {
            priceMap.put(p.symbol(), BigDecimal.valueOf(p.price()));
        }

        List<PriceAlert> activeAlerts = alertRepository.findByStatus(AlertStatus.ACTIVE);

        for (PriceAlert alert : activeAlerts) {
            BigDecimal currentPrice = priceMap.get(alert.getSymbol());
            if (currentPrice == null) {
                continue;
            }

            if (alert.isTriggered(currentPrice)) {
                alert.trigger(currentPrice);
                alertRepository.save(alert);

                sseEmitterManager.send(
                        alert.getUser().getId(),
                        AlertNotification.from(alert)
                );
                log.info("Alert triggered: {} {} {} at {}",
                        alert.getSymbol(), alert.getCondition(),
                        alert.getTargetPrice(), currentPrice);
            }
        }
    }

    public SseEmitter subscribe(Long userId) {
        return sseEmitterManager.createEmitter(userId);
    }

    private PriceAlert findAlertForUser(Long alertId, Long userId) {
        PriceAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ALERT_NOT_FOUND));
        if (!alert.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return alert;
    }
}
