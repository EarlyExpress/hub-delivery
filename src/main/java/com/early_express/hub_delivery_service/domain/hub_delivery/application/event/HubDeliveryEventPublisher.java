package com.early_express.hub_delivery_service.domain.hub_delivery.application.event;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;

/**
 * HubDelivery 이벤트 발행 인터페이스
 */
public interface HubDeliveryEventPublisher {

    /**
     * 구간 출발 이벤트 발행 (Track Service)
     */
    void publishSegmentDeparted(HubDelivery hubDelivery, HubSegment segment);

    /**
     * 구간 도착 이벤트 발행 (Track Service)
     */
    void publishSegmentArrived(HubDelivery hubDelivery, HubSegment segment);

    /**
     * 허브 배송 완료 이벤트 발행 (Order Service)
     */
    void publishHubDeliveryCompleted(HubDelivery hubDelivery);
}
