package com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 허브 구간 상태
 */
@Getter
@RequiredArgsConstructor
public enum HubSegmentStatus {

    PENDING("대기 중"),
    IN_TRANSIT("이동 중"),
    ARRIVED("도착"),
    FAILED("실패");

    private final String description;

    public boolean canDepart() {
        return this == PENDING;
    }

    public boolean canArrive() {
        return this == IN_TRANSIT;
    }

    public boolean isCompleted() {
        return this == ARRIVED;
    }

    public boolean isTerminal() {
        return this == ARRIVED || this == FAILED;
    }
}
