package com.stockpulse.infra.websocket;

import com.stockpulse.infra.external.dto.PriceData;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StompPricePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public StompPricePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishAll(List<PriceData> prices) {
        messagingTemplate.convertAndSend("/topic/prices/all", prices);
    }

    public void publish(PriceData price) {
        messagingTemplate.convertAndSend("/topic/prices/" + price.symbol(), price);
    }
}
