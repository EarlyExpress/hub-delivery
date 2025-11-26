package com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception;

import com.early_express.hub_delivery_service.global.presentation.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Hub Delivery 도메인 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum HubDeliveryErrorCode implements ErrorCode {

    // 조회 관련 (404)
    HUB_DELIVERY_NOT_FOUND("HUB_DELIVERY_001", "허브 배송 정보를 찾을 수 없습니다.", 404),
    HUB_SEGMENT_NOT_FOUND("HUB_DELIVERY_002", "허브 구간 정보를 찾을 수 없습니다.", 404),

    // 상태 관련 (400)
    INVALID_HUB_DELIVERY_STATUS("HUB_DELIVERY_101", "유효하지 않은 허브 배송 상태입니다.", 400),
    INVALID_STATUS_TRANSITION("HUB_DELIVERY_102", "허용되지 않는 상태 전환입니다.", 400),
    HUB_DELIVERY_ALREADY_COMPLETED("HUB_DELIVERY_103", "이미 완료된 허브 배송입니다.", 400),
    HUB_DELIVERY_ALREADY_FAILED("HUB_DELIVERY_104", "이미 실패한 허브 배송입니다.", 400),

    // 구간 관련 (400)
    INVALID_SEGMENT_INDEX("HUB_DELIVERY_201", "유효하지 않은 구간 순서입니다.", 400),
    SEGMENT_NOT_READY("HUB_DELIVERY_202", "이전 구간이 완료되지 않았습니다.", 400),
    SEGMENT_ALREADY_DEPARTED("HUB_DELIVERY_203", "이미 출발한 구간입니다.", 400),
    SEGMENT_ALREADY_ARRIVED("HUB_DELIVERY_204", "이미 도착한 구간입니다.", 400),
    ALL_SEGMENTS_COMPLETED("HUB_DELIVERY_205", "모든 구간이 이미 완료되었습니다.", 400),
    SEGMENT_CANNOT_ASSIGN("HUB_DELIVERY_206", "구간에 드라이버를 배정할 수 없습니다.", 400),

    // 배송 담당자 관련 (400)
    DRIVER_NOT_ASSIGNED("HUB_DELIVERY_301", "배송 담당자가 배정되지 않았습니다.", 400),
    DRIVER_ALREADY_ASSIGNED("HUB_DELIVERY_302", "이미 배송 담당자가 배정되어 있습니다.", 400),

    // 데이터 검증 (400)
    INVALID_ORDER_ID("HUB_DELIVERY_401", "유효하지 않은 주문 ID입니다.", 400),
    INVALID_HUB_ID("HUB_DELIVERY_402", "유효하지 않은 허브 ID입니다.", 400),
    INVALID_ROUTING_DATA("HUB_DELIVERY_403", "유효하지 않은 경로 데이터입니다.", 400),
    EMPTY_SEGMENTS("HUB_DELIVERY_404", "허브 구간이 비어있습니다.", 400),

    // 중복 관련 (409)
    HUB_DELIVERY_ALREADY_EXISTS("HUB_DELIVERY_501", "해당 주문의 허브 배송이 이미 존재합니다.", 409);

    private final String code;
    private final String message;
    private final int status;
}