package com.early_express.hub_delivery_service.domain.hub_delivery.presentation.internal;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.HubDeliveryCommandService;
import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.dto.HubDeliveryCommandDto.*;
import com.early_express.hub_delivery_service.domain.hub_delivery.presentation.internal.dto.request.HubDeliveryCreateRequest;
import com.early_express.hub_delivery_service.domain.hub_delivery.presentation.internal.dto.response.HubDeliveryCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Hub Delivery Internal Controller
 * 내부 서비스 간 통신용 (Order Service)
 */
@Slf4j
@RestController
@RequestMapping("/v1/hub-delivery/internal")
@RequiredArgsConstructor
public class HubDeliveryInternalController {

    private final HubDeliveryCommandService hubDeliveryCommandService;

    /**
     * 허브 배송 생성
     * POST /v1/hub-delivery/internal/deliveries
     */
    @PostMapping("/deliveries")
    public HubDeliveryCreateResponse createDelivery(
            @Valid @RequestBody HubDeliveryCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("[Internal] 허브 배송 생성 요청 - orderId: {}", request.getOrderId());

        CreateCommand command = request.toCommand(userId);
        CreateResult result = hubDeliveryCommandService.create(command);

        log.info("[Internal] 허브 배송 생성 완료 - hubDeliveryId: {}, orderId: {}",
                result.getHubDeliveryId(), result.getOrderId());

        return HubDeliveryCreateResponse.from(result);
    }

    /**
     * 허브 배송 취소 (보상 트랜잭션)
     * POST /v1/hub-delivery/internal/deliveries/{hubDeliveryId}/cancel
     */
    @PostMapping("/deliveries/{hubDeliveryId}/cancel")
    public HubDeliveryCreateResponse cancelDelivery(
            @PathVariable String hubDeliveryId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("[Internal] 허브 배송 취소 요청 - hubDeliveryId: {}", hubDeliveryId);

        CancelCommand command = CancelCommand.builder()
                .hubDeliveryId(hubDeliveryId)
                .cancelledBy(userId)
                .build();

        CreateResult result = hubDeliveryCommandService.cancel(command);

        log.info("[Internal] 허브 배송 취소 완료 - hubDeliveryId: {}", hubDeliveryId);

        return HubDeliveryCreateResponse.from(result);
    }
}
