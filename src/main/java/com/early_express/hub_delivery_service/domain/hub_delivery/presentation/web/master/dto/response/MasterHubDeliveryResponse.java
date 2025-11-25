package com.early_express.hub_delivery_service.domain.hub_delivery.presentation.web.master.dto.response;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.query.dto.HubDeliveryQueryDto.HubDeliveryResponse;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 마스터용 허브 배송 응답
 */
@Getter
@Builder
public class MasterHubDeliveryResponse {

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
    private LocalDateTime completedAt;
    private Long totalEstimatedDurationMin;
    private Long totalActualDurationMin;
    private LocalDateTime createdAt;

    /**
     * Query DTO → Presentation DTO 변환
     */
    public static MasterHubDeliveryResponse from(HubDeliveryResponse hubDelivery) {
        return MasterHubDeliveryResponse.builder()
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
                .completedAt(hubDelivery.getCompletedAt())
                .totalEstimatedDurationMin(hubDelivery.getTotalEstimatedDurationMin())
                .totalActualDurationMin(hubDelivery.getTotalActualDurationMin())
                .createdAt(hubDelivery.getCreatedAt())
                .build();
    }
}