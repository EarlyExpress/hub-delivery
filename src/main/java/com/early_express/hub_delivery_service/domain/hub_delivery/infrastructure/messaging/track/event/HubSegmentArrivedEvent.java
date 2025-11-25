package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.messaging.track.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 허브 구간 도착 이벤트
 * Hub Delivery Service → Track Service
 */
@Getter
@Builder
public class HubSegmentArrivedEvent {

    private String eventId;
    private String eventType;
    private String source;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String orderId;
    private String hubDeliveryId;
    private Integer segmentIndex;
    private String hubId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime arrivedAt;
}
