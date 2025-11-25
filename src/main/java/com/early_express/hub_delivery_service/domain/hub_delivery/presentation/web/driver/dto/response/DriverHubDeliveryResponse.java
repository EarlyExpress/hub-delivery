package com.early_express.hub_delivery_service.domain.hub_delivery.presentation.web.driver.dto.response;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.query.dto.HubDeliveryQueryDto.*;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 배송 담당자용 허브 배송 응답
 */
@Getter
@Builder
public class DriverHubDeliveryResponse {

    private String hubDeliveryId;
    private String orderId;
    private HubDeliveryStatus status;
    private Integer currentSegmentIndex;
    private Integer totalSegments;
    private Integer completedSegments;
    private LocalDateTime startedAt;
    private List<SegmentInfo> segments;

    @Getter
    @Builder
    public static class SegmentInfo {
        private Integer sequence;
        private String fromHubId;
        private String toHubId;
        private HubSegmentStatus status;
        private LocalDateTime departedAt;
        private LocalDateTime arrivedAt;
        private Long estimatedDurationMin;
        private Long actualDurationMin;
    }

    /**
     * Query DTO → Presentation DTO 변환
     */
    public static DriverHubDeliveryResponse from(HubDeliveryDetailResponse detail) {
        HubDeliveryResponse hubDelivery = detail.getHubDelivery();

        List<SegmentInfo> segments = detail.getSegments().stream()
                .map(segment -> SegmentInfo.builder()
                        .sequence(segment.getSequence())
                        .fromHubId(segment.getFromHubId())
                        .toHubId(segment.getToHubId())
                        .status(segment.getStatus())
                        .departedAt(segment.getDepartedAt())
                        .arrivedAt(segment.getArrivedAt())
                        .estimatedDurationMin(segment.getEstimatedDurationMin())
                        .actualDurationMin(segment.getActualDurationMin())
                        .build())
                .toList();

        return DriverHubDeliveryResponse.builder()
                .hubDeliveryId(hubDelivery.getHubDeliveryId())
                .orderId(hubDelivery.getOrderId())
                .status(hubDelivery.getStatus())
                .currentSegmentIndex(hubDelivery.getCurrentSegmentIndex())
                .totalSegments(hubDelivery.getTotalSegments())
                .completedSegments(hubDelivery.getCompletedSegments())
                .startedAt(hubDelivery.getStartedAt())
                .segments(segments)
                .build();
    }
}
