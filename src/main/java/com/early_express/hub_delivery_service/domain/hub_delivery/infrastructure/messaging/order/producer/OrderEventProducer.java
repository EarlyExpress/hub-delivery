package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.messaging.order.producer;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.messaging.order.event.HubDeliveryCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order 도메인 이벤트 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.hub-delivery-completed}")
    private String hubDeliveryCompletedTopic;

    /**
     * 허브 배송 완료 이벤트 발행
     */
    public void publishHubDeliveryCompleted(HubDelivery hubDelivery) {
        HubDeliveryCompletedEvent event = HubDeliveryCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("HUB_DELIVERY_COMPLETED")
                .source("hub-delivery-service")
                .timestamp(LocalDateTime.now())
                .orderId(hubDelivery.getOrderId())
                .hubDeliveryId(hubDelivery.getIdValue())
                .completedAt(hubDelivery.getCompletedAt())
                .totalActualDurationMin(hubDelivery.getTotalActualDurationMin())
                .build();

        kafkaTemplate.send(hubDeliveryCompletedTopic, hubDelivery.getOrderId(), event);

        log.info("[Order] HubDeliveryCompleted 이벤트 발행 - orderId: {}, hubDeliveryId: {}",
                hubDelivery.getOrderId(), hubDelivery.getIdValue());
    }
}
