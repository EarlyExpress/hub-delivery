package com.early_express.hub_delivery_service.domain.hub_delivery.presentation.web.master;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.query.HubDeliveryQueryService;
import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.query.dto.HubDeliveryQueryDto.HubDeliveryResponse;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import com.early_express.hub_delivery_service.domain.hub_delivery.presentation.web.master.dto.response.MasterHubDeliveryResponse;
import com.early_express.hub_delivery_service.global.common.dto.PageInfo;
import com.early_express.hub_delivery_service.global.presentation.dto.ApiResponse;
import com.early_express.hub_delivery_service.global.presentation.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Master Hub Delivery Controller
 * 마스터 관리자용 API
 */
@Slf4j
@RestController
@RequestMapping("/v1/hub-delivery/web/master")
@RequiredArgsConstructor
public class HubDeliveryMasterController {

    private final HubDeliveryQueryService queryService;

    /**
     * 전체 배송 목록 조회
     * GET /v1/hub-delivery/web/master/deliveries
     */
    @GetMapping("/deliveries")
    public ApiResponse<PageResponse<MasterHubDeliveryResponse>> getAllDeliveries(
            @RequestParam(required = false) HubDeliveryStatus status,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("마스터 배송 목록 조회 - status: {}", status);

        // TODO: roles 검증 (MASTER 권한 확인)

        Page<HubDeliveryResponse> queryResult = status != null
                ? queryService.findByStatus(status, pageable)
                : queryService.findAll(pageable);

        List<MasterHubDeliveryResponse> content = queryResult.getContent().stream()
                .map(MasterHubDeliveryResponse::from)
                .toList();

        return ApiResponse.success(PageResponse.of(content, PageInfo.of(queryResult)));
    }
}