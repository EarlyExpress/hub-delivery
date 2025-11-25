package com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * HubSegmentStatus Enum 테스트
 */
class HubSegmentStatusTest {

    @Test
    @DisplayName("PENDING 상태에서 출발 가능")
    void pending_canDepart_shouldReturnTrue() {
        // given
        HubSegmentStatus status = HubSegmentStatus.PENDING;

        // then
        assertThat(status.canDepart()).isTrue();
        assertThat(status.canArrive()).isFalse();
        assertThat(status.isCompleted()).isFalse();
        assertThat(status.isTerminal()).isFalse();
    }

    @Test
    @DisplayName("IN_TRANSIT 상태에서 도착 가능")
    void inTransit_canArrive_shouldReturnTrue() {
        // given
        HubSegmentStatus status = HubSegmentStatus.IN_TRANSIT;

        // then
        assertThat(status.canDepart()).isFalse();
        assertThat(status.canArrive()).isTrue();
        assertThat(status.isCompleted()).isFalse();
        assertThat(status.isTerminal()).isFalse();
    }

    @Test
    @DisplayName("ARRIVED 상태는 완료되고 종료 상태")
    void arrived_shouldBeCompletedAndTerminal() {
        // given
        HubSegmentStatus status = HubSegmentStatus.ARRIVED;

        // then
        assertThat(status.isCompleted()).isTrue();
        assertThat(status.isTerminal()).isTrue();
        assertThat(status.canDepart()).isFalse();
        assertThat(status.canArrive()).isFalse();
    }

    @Test
    @DisplayName("FAILED 상태는 종료 상태")
    void failed_shouldBeTerminal() {
        // given
        HubSegmentStatus status = HubSegmentStatus.FAILED;

        // then
        assertThat(status.isTerminal()).isTrue();
        assertThat(status.isCompleted()).isFalse();
    }
}