package com.early_express.hub_delivery_service.domain.hub_delivery.application.service.query.dto;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HubDelivery Query DTO
 */
public class HubDeliveryQueryDto {

    /**
     * 허브 배송 조회 응답
     */
    @Getter
    @Builder
    public static class HubDeliveryResponse {
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

        public static HubDeliveryResponse from(HubDelivery hubDelivery) {
            return HubDeliveryResponse.builder()
                    .hubDeliveryId(hubDelivery.getIdValue())
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

    /**
     * 허브 배송 상세 응답 (구간 정보 포함)
     */
    @Getter
    @Builder
    public static class HubDeliveryDetailResponse {
        private HubDeliveryResponse hubDelivery;
        private List<HubSegmentResponse> segments;

        public static HubDeliveryDetailResponse from(HubDelivery hubDelivery) {
            List<HubSegmentResponse> segmentResponses = hubDelivery.getSegments().stream()
                    .map(HubSegmentResponse::from)
                    .toList();

            return HubDeliveryDetailResponse.builder()
                    .hubDelivery(HubDeliveryResponse.from(hubDelivery))
                    .segments(segmentResponses)
                    .build();
        }
    }

    /**
     * 허브 구간 응답
     */
    @Getter
    @Builder
    public static class HubSegmentResponse {
        private Integer sequence;
        private String fromHubId;
        private String toHubId;
        private HubSegmentStatus status;
        private Long estimatedDistanceM;
        private Long estimatedDurationMin;
        private LocalDateTime departedAt;
        private LocalDateTime arrivedAt;
        private Long actualDurationMin;

        public static HubSegmentResponse from(HubSegment segment) {
            return HubSegmentResponse.builder()
                    .sequence(segment.getSequence())
                    .fromHubId(segment.getFromHubId())
                    .toHubId(segment.getToHubId())
                    .status(segment.getStatus())
                    .estimatedDistanceM(segment.getEstimatedDistanceM())
                    .estimatedDurationMin(segment.getEstimatedDurationMin())
                    .departedAt(segment.getDepartedAt())
                    .arrivedAt(segment.getArrivedAt())
                    .actualDurationMin(segment.getActualDurationMin())
                    .build();
        }
    }
}
