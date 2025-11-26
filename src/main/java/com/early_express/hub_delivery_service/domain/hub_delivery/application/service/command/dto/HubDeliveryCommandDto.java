package com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HubDelivery Command DTO
 */
public class HubDeliveryCommandDto {

    /**
     * 허브 배송 생성 Command
     */
    @Getter
    @Builder
    public static class CreateCommand {
        private String orderId;
        private String originHubId;
        private String destinationHubId;
        private List<String> routeHubs;
        private String routeInfoJson;
        private LocalDateTime departureDeadline;
        private LocalDateTime estimatedArrivalTime;
        private String createdBy;
    }

    /**
     * 구간 드라이버 배정 Command
     * Track Service에서 호출
     */
    @Getter
    @Builder
    public static class AssignDriverForSegmentCommand {
        private String hubDeliveryId;
        private Integer segmentIndex;
        private String requestedBy;
    }

    /**
     * 구간 출발 Command
     */
    @Getter
    @Builder
    public static class DepartSegmentCommand {
        private String hubDeliveryId;
        private Integer segmentIndex;
        private String driverId;
    }

    /**
     * 구간 도착 Command
     */
    @Getter
    @Builder
    public static class ArriveSegmentCommand {
        private String hubDeliveryId;
        private Integer segmentIndex;
        private String driverId;
    }

    /**
     * 배송 취소 Command
     */
    @Getter
    @Builder
    public static class CancelCommand {
        private String hubDeliveryId;
        private String cancelledBy;
    }

    /**
     * 생성 결과 Response
     */
    @Getter
    @Builder
    public static class CreateResult {
        private String hubDeliveryId;
        private String orderId;
        private String status;
        private String message;

        public static CreateResult success(String hubDeliveryId, String orderId, String status) {
            return CreateResult.builder()
                    .hubDeliveryId(hubDeliveryId)
                    .orderId(orderId)
                    .status(status)
                    .message("허브 배송이 생성되었습니다.")
                    .build();
        }

        public static CreateResult cancelled(String hubDeliveryId, String orderId) {
            return CreateResult.builder()
                    .hubDeliveryId(hubDeliveryId)
                    .orderId(orderId)
                    .status("CANCELLED")
                    .message("허브 배송이 취소되었습니다.")
                    .build();
        }
    }

    /**
     * 구간 드라이버 배정 결과
     */
    @Getter
    @Builder
    public static class AssignDriverResult {
        private String hubDeliveryId;
        private Integer segmentIndex;
        private String driverId;
        private String driverName;
        private String status;
        private boolean success;
        private String message;

        public static AssignDriverResult success(String hubDeliveryId, Integer segmentIndex,
                                                 String driverId, String driverName) {
            return AssignDriverResult.builder()
                    .hubDeliveryId(hubDeliveryId)
                    .segmentIndex(segmentIndex)
                    .driverId(driverId)
                    .driverName(driverName)
                    .status("ASSIGNED")
                    .success(true)
                    .message("드라이버가 배정되었습니다.")
                    .build();
        }

        public static AssignDriverResult failed(String hubDeliveryId, Integer segmentIndex,
                                                String reason) {
            return AssignDriverResult.builder()
                    .hubDeliveryId(hubDeliveryId)
                    .segmentIndex(segmentIndex)
                    .status("FAILED")
                    .success(false)
                    .message(reason)
                    .build();
        }
    }
}
