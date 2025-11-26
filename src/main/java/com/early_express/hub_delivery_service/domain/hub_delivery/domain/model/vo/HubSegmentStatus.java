package com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 허브 구간 상태
 *
 * 상태 흐름:
 * PENDING → ASSIGNED → IN_TRANSIT → ARRIVED
 *                ↓           ↓
 *             FAILED      FAILED
 */
@Getter
@RequiredArgsConstructor
public enum HubSegmentStatus {

    PENDING("대기 중"),           // 드라이버 미배정
    ASSIGNED("배정 완료"),        // 드라이버 배정됨, 출발 대기
    IN_TRANSIT("이동 중"),        // 출발함
    ARRIVED("도착"),              // 도착 완료
    FAILED("실패");               // 실패

    private final String description;

    /**
     * 드라이버 배정 가능 여부
     * PENDING 상태에서만 배정 가능
     */
    public boolean canAssign() {
        return this == PENDING;
    }

    /**
     * 출발 가능 여부
     * ASSIGNED 상태에서만 출발 가능
     */
    public boolean canDepart() {
        return this == ASSIGNED;
    }

    /**
     * 도착 처리 가능 여부
     * IN_TRANSIT 상태에서만 도착 가능
     */
    public boolean canArrive() {
        return this == IN_TRANSIT;
    }

    /**
     * 완료 상태 여부
     */
    public boolean isCompleted() {
        return this == ARRIVED;
    }

    /**
     * 종료 상태 여부 (더 이상 상태 변경 불가)
     */
    public boolean isTerminal() {
        return this == ARRIVED || this == FAILED;
    }
}