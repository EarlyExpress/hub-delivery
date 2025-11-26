package com.early_express.hub_delivery_service.domain.hub_delivery.presentation.internal.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구간 드라이버 배정 요청 DTO
 * (현재는 Path Variable로 처리하므로 빈 클래스, 향후 확장용)
 */
@Getter
@NoArgsConstructor
public class AssignDriverForSegmentRequest {
    // Path Variable로 hubDeliveryId, segmentIndex를 받으므로
    // 추가 데이터가 필요할 경우 여기에 정의
}