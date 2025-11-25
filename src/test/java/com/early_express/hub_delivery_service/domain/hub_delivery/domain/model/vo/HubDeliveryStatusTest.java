package com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * HubDeliveryStatus Enum 테스트
 */
class HubDeliveryStatusTest {

    @Test
    @DisplayName("CREATED 상태는 배송 담당자 배정 가능")
    void created_canAssignDriver_shouldReturnTrue() {
        // given
        HubDeliveryStatus status = HubDeliveryStatus.CREATED;

        // then
        assertThat(status.canAssignDriver()).isTrue();
        assertThat(status.isInProgress()).isFalse();
        assertThat(status.isTerminal()).isFalse();
    }

    @Test
    @DisplayName("WAITING_DRIVER 상태는 진행 중이고 시작 가능")
    void waitingDriver_shouldBeInProgressAndCanStart() {
        // given
        HubDeliveryStatus status = HubDeliveryStatus.WAITING_DRIVER;

        // then
        assertThat(status.isInProgress()).isTrue();
        assertThat(status.canStart()).isTrue();
        assertThat(status.canAssignDriver()).isTrue();
    }

    @Test
    @DisplayName("IN_PROGRESS 상태는 진행 중")
    void inProgress_shouldBeInProgress() {
        // given
        HubDeliveryStatus status = HubDeliveryStatus.IN_PROGRESS;

        // then
        assertThat(status.isInProgress()).isTrue();
        assertThat(status.isTerminal()).isFalse();
    }

    @Test
    @DisplayName("COMPLETED 상태는 종료 상태")
    void completed_shouldBeTerminal() {
        // given
        HubDeliveryStatus status = HubDeliveryStatus.COMPLETED;

        // then
        assertThat(status.isTerminal()).isTrue();
        assertThat(status.isInProgress()).isFalse();
        assertThat(status.canAssignDriver()).isFalse();
    }

    @Test
    @DisplayName("FAILED 상태는 종료 상태")
    void failed_shouldBeTerminal() {
        // given
        HubDeliveryStatus status = HubDeliveryStatus.FAILED;

        // then
        assertThat(status.isTerminal()).isTrue();
        assertThat(status.isInProgress()).isFalse();
    }
}