package com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 허브 배송 상태
 */
@Getter
@RequiredArgsConstructor
public enum HubDeliveryStatus {

    CREATED("생성됨"),
    WAITING_DRIVER("배송 담당자 대기 중"),
    IN_PROGRESS("배송 중"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String description;

    public boolean isInProgress() {
        return this == WAITING_DRIVER || this == IN_PROGRESS;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }

    public boolean canAssignDriver() {
        return this == CREATED || this == WAITING_DRIVER;
    }

    public boolean canStart() {
        return this == WAITING_DRIVER;
    }
}
