package com.early_express.hub_delivery_service.domain.hub_delivery.presentation.internal.dto.response;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.dto.HubDeliveryCommandDto.CreateResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 허브 배송 생성 응답 DTO (Internal)
 * Hub Delivery Service → Order Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubDeliveryCreateResponse {

    private String hubDeliveryId;
    private String orderId;
    private String status;
    private String message;

    /**
     * Command Result → Response 변환
     */
    public static HubDeliveryCreateResponse from(CreateResult result) {
        return HubDeliveryCreateResponse.builder()
                .hubDeliveryId(result.getHubDeliveryId())
                .orderId(result.getOrderId())
                .status(result.getStatus())
                .message(result.getMessage())
                .build();
    }

    public boolean isSuccess() {
        return hubDeliveryId != null && !hubDeliveryId.isBlank();
    }
}
