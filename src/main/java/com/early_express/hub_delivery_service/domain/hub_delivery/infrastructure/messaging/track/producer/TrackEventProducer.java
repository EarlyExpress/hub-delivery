package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.messaging.track.producer;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.messaging.track.event.HubSegmentArrivedEvent;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.messaging.track.event.HubSegmentDepartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Track 도메인 이벤트 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrackEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.hub-segment-departed}")
    private String hubSegmentDepartedTopic;

    @Value("${spring.kafka.topic.hub-segment-arrived}")
    private String hubSegmentArrivedTopic;

    /**
     * 허브 구간 출발 이벤트 발행
     */
    public void publishSegmentDeparted(HubDelivery hubDelivery, HubSegment segment) {
        HubSegmentDepartedEvent event = HubSegmentDepartedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("HUB_SEGMENT_DEPARTED")
                .source("hub-delivery-service")
                .timestamp(LocalDateTime.now())
                .orderId(hubDelivery.getOrderId())
                .hubDeliveryId(hubDelivery.getIdValue())
                .segmentIndex(segment.getSequence())
                .fromHubId(segment.getFromHubId())
                .toHubId(segment.getToHubId())
                .departedAt(segment.getDepartedAt())
                .build();

        kafkaTemplate.send(hubSegmentDepartedTopic, hubDelivery.getOrderId(), event);

        log.info("[Track] HubSegmentDeparted 이벤트 발행 - orderId: {}, segment: {}, from: {} → to: {}",
                hubDelivery.getOrderId(), segment.getSequence(), segment.getFromHubId(), segment.getToHubId());
    }

    /**
     * 허브 구간 도착 이벤트 발행
     */
    public void publishSegmentArrived(HubDelivery hubDelivery, HubSegment segment) {
        HubSegmentArrivedEvent event = HubSegmentArrivedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("HUB_SEGMENT_ARRIVED")
                .source("hub-delivery-service")
                .timestamp(LocalDateTime.now())
                .orderId(hubDelivery.getOrderId())
                .hubDeliveryId(hubDelivery.getIdValue())
                .segmentIndex(segment.getSequence())
                .hubId(segment.getToHubId())
                .arrivedAt(segment.getArrivedAt())
                .build();

        kafkaTemplate.send(hubSegmentArrivedTopic, hubDelivery.getOrderId(), event);

        log.info("[Track] HubSegmentArrived 이벤트 발행 - orderId: {}, segment: {}, hubId: {}",
                hubDelivery.getOrderId(), segment.getSequence(), segment.getToHubId());
    }
}
