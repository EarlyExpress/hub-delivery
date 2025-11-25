package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.messaging;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.event.HubDeliveryEventPublisher;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.messaging.order.producer.OrderEventProducer;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.messaging.track.producer.TrackEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * HubDelivery 이벤트 발행 구현체
 */
@Component
@RequiredArgsConstructor
public class HubDeliveryEventPublisherImpl implements HubDeliveryEventPublisher {

    private final TrackEventProducer trackEventProducer;
    private final OrderEventProducer orderEventProducer;

    @Override
    public void publishSegmentDeparted(HubDelivery hubDelivery, HubSegment segment) {
        trackEventProducer.publishSegmentDeparted(hubDelivery, segment);
    }

    @Override
    public void publishSegmentArrived(HubDelivery hubDelivery, HubSegment segment) {
        trackEventProducer.publishSegmentArrived(hubDelivery, segment);
    }

    @Override
    public void publishHubDeliveryCompleted(HubDelivery hubDelivery) {
        orderEventProducer.publishHubDeliveryCompleted(hubDelivery);
    }
}
