package com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * HubSegment 값 객체 테스트
 */
class HubSegmentTest {

    @Test
    @DisplayName("새 구간 생성 성공")
    void create_shouldCreatePendingSegment() {
        // given
        Integer sequence = 0;
        String fromHubId = "hub-1";
        String toHubId = "hub-2";
        Long estimatedDistanceM = 10000L;
        Long estimatedDurationMin = 30L;

        // when
        HubSegment segment = HubSegment.create(sequence, fromHubId, toHubId,
                estimatedDistanceM, estimatedDurationMin);

        // then
        assertThat(segment.getSequence()).isEqualTo(sequence);
        assertThat(segment.getFromHubId()).isEqualTo(fromHubId);
        assertThat(segment.getToHubId()).isEqualTo(toHubId);
        assertThat(segment.getEstimatedDistanceM()).isEqualTo(estimatedDistanceM);
        assertThat(segment.getEstimatedDurationMin()).isEqualTo(estimatedDurationMin);
        assertThat(segment.getStatus()).isEqualTo(HubSegmentStatus.PENDING);
        assertThat(segment.isPending()).isTrue();
    }

    @Test
    @DisplayName("PENDING 상태에서 출발 성공")
    void depart_fromPending_shouldSucceed() {
        // given
        HubSegment segment = HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L);

        // when
        HubSegment departedSegment = segment.depart();

        // then
        assertThat(departedSegment.getStatus()).isEqualTo(HubSegmentStatus.IN_TRANSIT);
        assertThat(departedSegment.getDepartedAt()).isNotNull();
        assertThat(departedSegment.isInTransit()).isTrue();
    }

    @Test
    @DisplayName("IN_TRANSIT 상태에서 도착 성공")
    void arrive_fromInTransit_shouldSucceed() {
        // given
        HubSegment segment = HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L);
        HubSegment departedSegment = segment.depart();

        // when
        HubSegment arrivedSegment = departedSegment.arrive();

        // then
        assertThat(arrivedSegment.getStatus()).isEqualTo(HubSegmentStatus.ARRIVED);
        assertThat(arrivedSegment.getArrivedAt()).isNotNull();
        assertThat(arrivedSegment.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("이미 출발한 구간에서 다시 출발 시 예외 발생")
    void depart_alreadyDeparted_shouldThrowException() {
        // given
        HubSegment segment = HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L);
        HubSegment departedSegment = segment.depart();

        // when & then
        assertThatThrownBy(departedSegment::depart)
                .isInstanceOf(HubDeliveryException.class);
    }

    @Test
    @DisplayName("PENDING 상태에서 도착 시 예외 발생")
    void arrive_fromPending_shouldThrowException() {
        // given
        HubSegment segment = HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L);

        // when & then
        assertThatThrownBy(segment::arrive)
                .isInstanceOf(HubDeliveryException.class);
    }

    @Test
    @DisplayName("구간 실패 처리 성공")
    void fail_shouldSetStatusToFailed() {
        // given
        HubSegment segment = HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L);

        // when
        HubSegment failedSegment = segment.fail();

        // then
        assertThat(failedSegment.getStatus()).isEqualTo(HubSegmentStatus.FAILED);
    }
}