package com.early_express.hub_delivery_service.domain.hub_delivery.presentation.internal;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.HubDeliveryCommandService;
import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.dto.HubDeliveryCommandDto.*;
import com.early_express.hub_delivery_service.domain.hub_delivery.presentation.internal.dto.request.AssignDriverForSegmentRequest;
import com.early_express.hub_delivery_service.domain.hub_delivery.presentation.internal.dto.request.HubDeliveryCreateRequest;
import com.early_express.hub_delivery_service.domain.hub_delivery.presentation.internal.dto.response.AssignDriverForSegmentResponse;
import com.early_express.hub_delivery_service.domain.hub_delivery.presentation.internal.dto.response.HubDeliveryCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Hub Delivery Internal Controller
 *
 * 내부 서비스 간 통신용 API
 * - Order Service: 배송 생성/취소
 * - Track Service: 구간 드라이버 배정
 */
@Slf4j
@RestController
@RequestMapping("/v1/hub-delivery/internal")
@RequiredArgsConstructor
public class HubDeliveryInternalController {

    private final HubDeliveryCommandService hubDeliveryCommandService;

    /**
     * 허브 배송 생성
     *
     * Order Saga에서 호출합니다.
     * 경로 정보를 받아 HubDelivery와 각 구간(HubSegment)을 생성합니다.
     * 드라이버 배정은 하지 않습니다.
     *
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
     * 구간 드라이버 배정
     *
     * Track Service에서 호출합니다.
     * 특정 구간에 드라이버를 배정하고 출발 처리합니다.
     *
     * POST /v1/hub-delivery/internal/deliveries/{hubDeliveryId}/segments/{segmentIndex}/assign-driver
     */
    @PostMapping("/deliveries/{hubDeliveryId}/segments/{segmentIndex}/assign-driver")
    public AssignDriverForSegmentResponse assignDriverForSegment(
            @PathVariable String hubDeliveryId,
            @PathVariable Integer segmentIndex,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("[Internal] 구간 드라이버 배정 요청 - hubDeliveryId: {}, segment: {}",
                hubDeliveryId, segmentIndex);

        AssignDriverForSegmentCommand command = AssignDriverForSegmentCommand.builder()
                .hubDeliveryId(hubDeliveryId)
                .segmentIndex(segmentIndex)
                .requestedBy(userId)
                .build();

        AssignDriverResult result = hubDeliveryCommandService.assignDriverForSegment(command);

        log.info("[Internal] 구간 드라이버 배정 결과 - hubDeliveryId: {}, segment: {}, success: {}",
                hubDeliveryId, segmentIndex, result.isSuccess());

        return AssignDriverForSegmentResponse.from(result);
    }

    /**
     * 허브 배송 취소 (보상 트랜잭션)
     *
     * Order Saga 보상 트랜잭션에서 호출합니다.
     * 배정된 드라이버들에게 취소 통지 후 배송을 취소합니다.
     *
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