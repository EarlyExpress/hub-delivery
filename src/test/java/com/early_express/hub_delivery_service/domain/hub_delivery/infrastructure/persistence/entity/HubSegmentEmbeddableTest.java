package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.persistence.entity;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * HubSegmentEmbeddable 테스트
 */
class HubSegmentEmbeddableTest {

    @Test
    @DisplayName("도메인에서 Embeddable로 변환 성공")
    void from_shouldConvertFromDomain() {
        // given
        HubSegment segment = HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L);

        // when
        HubSegmentEmbeddable embeddable = HubSegmentEmbeddable.from(segment);

        // then
        assertThat(embeddable.getSequence()).isEqualTo(0);
        assertThat(embeddable.getFromHubId()).isEqualTo("hub-1");
        assertThat(embeddable.getToHubId()).isEqualTo("hub-2");
        assertThat(embeddable.getEstimatedDistanceM()).isEqualTo(10000L);
        assertThat(embeddable.getEstimatedDurationMin()).isEqualTo(30L);
        assertThat(embeddable.getStatus()).isEqualTo(HubSegmentStatus.PENDING);
    }

    @Test
    @DisplayName("Embeddable에서 도메인으로 변환 성공")
    void toDomain_shouldConvertToDomain() {
        // given
        HubSegment originalSegment = HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L);
        HubSegmentEmbeddable embeddable = HubSegmentEmbeddable.from(originalSegment);

        // when
        HubSegment convertedSegment = embeddable.toDomain();

        // then
        assertThat(convertedSegment.getSequence()).isEqualTo(originalSegment.getSequence());
        assertThat(convertedSegment.getFromHubId()).isEqualTo(originalSegment.getFromHubId());
        assertThat(convertedSegment.getToHubId()).isEqualTo(originalSegment.getToHubId());
        assertThat(convertedSegment.getEstimatedDistanceM()).isEqualTo(originalSegment.getEstimatedDistanceM());
        assertThat(convertedSegment.getStatus()).isEqualTo(originalSegment.getStatus());
    }

    @Test
    @DisplayName("출발/도착 상태의 세그먼트 변환 성공")
    void fromAndToDomain_withDepartedAndArrivedSegment_shouldPreserveState() {
        // given
        HubSegment segment = HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L);
        HubSegment departedSegment = segment.depart();
        HubSegment arrivedSegment = departedSegment.arrive();

        // when
        HubSegmentEmbeddable embeddable = HubSegmentEmbeddable.from(arrivedSegment);
        HubSegment convertedSegment = embeddable.toDomain();

        // then
        assertThat(convertedSegment.getStatus()).isEqualTo(HubSegmentStatus.ARRIVED);
        assertThat(convertedSegment.getDepartedAt()).isNotNull();
        assertThat(convertedSegment.getArrivedAt()).isNotNull();
    }
}