package com.early_express.hub_delivery_service.domain.hub_delivery.presentation.internal.dto.request;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.dto.HubDeliveryCommandDto.CreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 허브 배송 생성 요청 DTO (Internal)
 * Order Service → Hub Delivery Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubDeliveryCreateRequest {

    @NotBlank(message = "주문 ID는 필수입니다.")
    private String orderId;

    @NotBlank(message = "출발 허브 ID는 필수입니다.")
    private String originHubId;

    @NotBlank(message = "도착 허브 ID는 필수입니다.")
    private String destinationHubId;

    @NotEmpty(message = "경유 허브 목록은 필수입니다.")
    private List<String> routeHubs;

    private String routeInfoJson;

    private String departureDeadline;

    private String estimatedArrivalTime;

    /**
     * Request → Command 변환
     */
    public CreateCommand toCommand(String createdBy) {
        return CreateCommand.builder()
                .orderId(this.orderId)
                .originHubId(this.originHubId)
                .destinationHubId(this.destinationHubId)
                .routeHubs(this.routeHubs)
                .routeInfoJson(this.routeInfoJson)
                .departureDeadline(parseDateTime(this.departureDeadline))
                .estimatedArrivalTime(parseDateTime(this.estimatedArrivalTime))
                .createdBy(createdBy)
                .build();
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}
