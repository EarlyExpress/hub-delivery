package com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Hub Delivery ID 값 객체
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubDeliveryId {

    private String value;

    private HubDeliveryId(String value) {
        this.value = value;
    }

    public static HubDeliveryId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("HubDeliveryId는 null이거나 빈 값일 수 없습니다.");
        }
        return new HubDeliveryId(value);
    }
}