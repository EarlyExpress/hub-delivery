package com.early_express.hub_delivery_service.domain.hub_delivery.presentation.internal.dto.response;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.dto.HubDeliveryCommandDto.AssignDriverResult;
import lombok.Builder;
import lombok.Getter;

/**
 * 구간 드라이버 배정 응답 DTO
 */
@Getter
@Builder
public class AssignDriverForSegmentResponse {

    private String hubDeliveryId;
    private Integer segmentIndex;
    private String driverId;
    private String driverName;
    private String status;
    private boolean success;
    private String message;

    public static AssignDriverForSegmentResponse from(AssignDriverResult result) {
        return AssignDriverForSegmentResponse.builder()
                .hubDeliveryId(result.getHubDeliveryId())
                .segmentIndex(result.getSegmentIndex())
                .driverId(result.getDriverId())
                .driverName(result.getDriverName())
                .status(result.getStatus())
                .success(result.isSuccess())
                .message(result.getMessage())
                .build();
    }
}