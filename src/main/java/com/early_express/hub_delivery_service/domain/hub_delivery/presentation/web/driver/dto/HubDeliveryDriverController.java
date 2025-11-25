package com.early_express.hub_delivery_service.domain.hub_delivery.presentation.web.driver.dto;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.HubDeliveryCommandService;
import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.dto.HubDeliveryCommandDto.*;
import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.query.HubDeliveryQueryService;
import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.query.dto.HubDeliveryQueryDto.HubDeliveryDetailResponse;
import com.early_express.hub_delivery_service.domain.hub_delivery.presentation.web.driver.dto.response.DriverHubDeliveryResponse;
import com.early_express.hub_delivery_service.global.presentation.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Driver Hub Delivery Controller
 * 배송 담당자용 API
 */
@Slf4j
@RestController
@RequestMapping("/v1/hub-delivery/web/drivers")
@RequiredArgsConstructor
public class HubDeliveryDriverController {

    private final HubDeliveryCommandService commandService;
    private final HubDeliveryQueryService queryService;

    /**
     * 내 배송 상세 조회
     * GET /v1/hub-delivery/web/drivers/deliveries/{hubDeliveryId}
     */
    @GetMapping("/deliveries/{hubDeliveryId}")
    public ApiResponse<DriverHubDeliveryResponse> getMyDelivery(
            @PathVariable String hubDeliveryId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles) {

        log.info("배송 담당자 배송 조회 - hubDeliveryId: {}, driverId: {}", hubDeliveryId, userId);

        // TODO: roles 검증 (DRIVER 권한 확인)

        HubDeliveryDetailResponse detail = queryService.findById(hubDeliveryId);

        // TODO: driverId 일치 여부 확인

        DriverHubDeliveryResponse response = DriverHubDeliveryResponse.from(detail);

        return ApiResponse.success(response);
    }

    /**
     * 구간 출발 처리
     * PUT /v1/hub-delivery/web/drivers/deliveries/{hubDeliveryId}/segments/{segmentIndex}/depart
     */
    @PutMapping("/deliveries/{hubDeliveryId}/segments/{segmentIndex}/depart")
    public ApiResponse<Void> departSegment(
            @PathVariable String hubDeliveryId,
            @PathVariable Integer segmentIndex,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles) {

        log.info("구간 출발 처리 - hubDeliveryId: {}, segment: {}, driverId: {}",
                hubDeliveryId, segmentIndex, userId);

        // TODO: roles 검증 (DRIVER 권한 확인)

        DepartSegmentCommand command = DepartSegmentCommand.builder()
                .hubDeliveryId(hubDeliveryId)
                .segmentIndex(segmentIndex)
                .driverId(userId)
                .build();

        commandService.departSegment(command);

        return ApiResponse.success();
    }

    /**
     * 구간 도착 처리
     * PUT /v1/hub-delivery/web/drivers/deliveries/{hubDeliveryId}/segments/{segmentIndex}/arrive
     */
    @PutMapping("/deliveries/{hubDeliveryId}/segments/{segmentIndex}/arrive")
    public ApiResponse<Void> arriveSegment(
            @PathVariable String hubDeliveryId,
            @PathVariable Integer segmentIndex,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles) {

        log.info("구간 도착 처리 - hubDeliveryId: {}, segment: {}, driverId: {}",
                hubDeliveryId, segmentIndex, userId);

        // TODO: roles 검증 (DRIVER 권한 확인)

        ArriveSegmentCommand command = ArriveSegmentCommand.builder()
                .hubDeliveryId(hubDeliveryId)
                .segmentIndex(segmentIndex)
                .driverId(userId)
                .build();

        commandService.arriveSegment(command);

        return ApiResponse.success();
    }
}
