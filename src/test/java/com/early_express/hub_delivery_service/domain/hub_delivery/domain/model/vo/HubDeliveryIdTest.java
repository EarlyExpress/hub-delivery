package com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * HubDeliveryId 값 객체 테스트
 */
class HubDeliveryIdTest {

    @Test
    @DisplayName("유효한 값으로 HubDeliveryId 생성 성공")
    void createHubDeliveryId_withValidValue_shouldSucceed() {
        // given
        String value = "test-hub-delivery-id";

        // when
        HubDeliveryId hubDeliveryId = HubDeliveryId.of(value);

        // then
        assertThat(hubDeliveryId).isNotNull();
        assertThat(hubDeliveryId.getValue()).isEqualTo(value);
    }

    @Test
    @DisplayName("null 값으로 HubDeliveryId 생성 시 예외 발생")
    void createHubDeliveryId_withNullValue_shouldThrowException() {
        // when & then
        assertThatThrownBy(() -> HubDeliveryId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null이거나 빈 값");
    }

    @Test
    @DisplayName("빈 값으로 HubDeliveryId 생성 시 예외 발생")
    void createHubDeliveryId_withBlankValue_shouldThrowException() {
        // when & then
        assertThatThrownBy(() -> HubDeliveryId.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null이거나 빈 값");
    }

    @Test
    @DisplayName("동일한 값을 가진 HubDeliveryId는 동등해야 함")
    void equals_withSameValue_shouldBeEqual() {
        // given
        HubDeliveryId id1 = HubDeliveryId.of("same-id");
        HubDeliveryId id2 = HubDeliveryId.of("same-id");

        // then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}