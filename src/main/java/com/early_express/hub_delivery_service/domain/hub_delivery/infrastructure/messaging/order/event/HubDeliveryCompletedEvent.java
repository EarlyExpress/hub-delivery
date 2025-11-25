package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.messaging.order.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 허브 배송 완료 이벤트
 * Hub Delivery Service → Order Service
 */
@Getter
@Builder
public class HubDeliveryCompletedEvent {

    private String eventId;
    private String eventType;
    private String source;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String orderId;
    private String hubDeliveryId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    private Long totalActualDurationMin;
}
