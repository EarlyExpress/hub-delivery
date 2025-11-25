package com.early_express.hub_delivery_service.domain.hub_delivery.presentation.web.hubmanager.dto.response;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.query.dto.HubDeliveryQueryDto.HubDeliveryResponse;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 허브 관리자용 허브 배송 간단 응답
 */
@Getter
@Builder
public class HubManagerHubDeliveryResponse {

    private String hubDeliveryId;
    private String orderId;
    private String originHubId;
    private String destinationHubId;
    private HubDeliveryStatus status;
    private String driverId;
    private Integer currentSegmentIndex;
    private Integer totalSegments;
    private Integer completedSegments;
    private LocalDateTime startedAt;
    private LocalDateTime createdAt;

    /**
     * Query DTO → Presentation DTO 변환
     */
    public static HubManagerHubDeliveryResponse from(HubDeliveryResponse hubDelivery) {
        return HubManagerHubDeliveryResponse.builder()
                .hubDeliveryId(hubDelivery.getHubDeliveryId())
                .orderId(hubDelivery.getOrderId())
                .originHubId(hubDelivery.getOriginHubId())
                .destinationHubId(hubDelivery.getDestinationHubId())
                .status(hubDelivery.getStatus())
                .driverId(hubDelivery.getDriverId())
                .currentSegmentIndex(hubDelivery.getCurrentSegmentIndex())
                .totalSegments(hubDelivery.getTotalSegments())
                .completedSegments(hubDelivery.getCompletedSegments())
                .startedAt(hubDelivery.getStartedAt())
                .createdAt(hubDelivery.getCreatedAt())
                .build();
    }
}

