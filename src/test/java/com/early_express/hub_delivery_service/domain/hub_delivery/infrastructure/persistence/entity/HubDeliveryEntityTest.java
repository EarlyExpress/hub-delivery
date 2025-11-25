package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.persistence.entity;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * HubDeliveryEntity 테스트
 */
class HubDeliveryEntityTest {

    private HubDelivery testHubDelivery;

    @BeforeEach
    void setUp() {
        List<HubSegment> segments = List.of(
                HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L),
                HubSegment.create(1, "hub-2", "hub-3", 15000L, 45L)
        );

        testHubDelivery = HubDelivery.create(
                "order-1",
                "hub-1",
                "hub-3",
                segments,
                "system"
        );
    }

    @Test
    @DisplayName("도메인에서 엔티티로 변환 성공 (새 엔티티)")
    void fromDomain_newEntity_shouldGenerateId() {
        // when
        HubDeliveryEntity entity = HubDeliveryEntity.fromDomain(testHubDelivery);

        // then
        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getOrderId()).isEqualTo("order-1");
        assertThat(entity.getOriginHubId()).isEqualTo("hub-1");
        assertThat(entity.getDestinationHubId()).isEqualTo("hub-3");
        assertThat(entity.getStatus()).isEqualTo(HubDeliveryStatus.CREATED);
        assertThat(entity.getSegments()).hasSize(2);
    }

    @Test
    @DisplayName("엔티티에서 도메인으로 변환 성공")
    void toDomain_shouldConvertToDomain() {
        // given
        HubDeliveryEntity entity = HubDeliveryEntity.fromDomain(testHubDelivery);

        // when
        HubDelivery convertedDomain = entity.toDomain();

        // then
        assertThat(convertedDomain.getIdValue()).isEqualTo(entity.getId());
        assertThat(convertedDomain.getOrderId()).isEqualTo(entity.getOrderId());
        assertThat(convertedDomain.getStatus()).isEqualTo(entity.getStatus());
        assertThat(convertedDomain.getTotalSegments()).isEqualTo(entity.getSegments().size());
    }

    @Test
    @DisplayName("도메인 변경사항 엔티티에 업데이트 성공")
    void updateFromDomain_shouldUpdateEntity() {
        // given
        HubDeliveryEntity entity = HubDeliveryEntity.fromDomain(testHubDelivery);
        HubDelivery domain = entity.toDomain();

        // 도메인 상태 변경
        domain.assignDriver("driver-1");
        domain.departSegment(0);

        // when
        entity.updateFromDomain(domain);

        // then
        assertThat(entity.getStatus()).isEqualTo(HubDeliveryStatus.IN_PROGRESS);
        assertThat(entity.getDriverId()).isEqualTo("driver-1");
        assertThat(entity.getCurrentSegmentIndex()).isEqualTo(0);
    }

    @Test
    @DisplayName("세그먼트 변환이 올바르게 수행됨")
    void fromDomain_segmentsShouldBeConverted() {
        // when
        HubDeliveryEntity entity = HubDeliveryEntity.fromDomain(testHubDelivery);

        // then
        assertThat(entity.getSegments()).hasSize(2);
        assertThat(entity.getSegments().get(0).getSequence()).isEqualTo(0);
        assertThat(entity.getSegments().get(0).getFromHubId()).isEqualTo("hub-1");
        assertThat(entity.getSegments().get(1).getSequence()).isEqualTo(1);
        assertThat(entity.getSegments().get(1).getFromHubId()).isEqualTo("hub-2");
    }

    @Test
    @DisplayName("ID 불일치 시 업데이트 예외 발생")
    void updateFromDomain_withMismatchedId_shouldThrowException() {
        // given
        HubDeliveryEntity entity = HubDeliveryEntity.fromDomain(testHubDelivery);

        // 다른 ID를 가진 도메인 생성
        HubDelivery anotherDomain = HubDelivery.create(
                "order-2",
                "hub-1",
                "hub-3",
                List.of(HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L)),
                "system"
        );
        HubDeliveryEntity anotherEntity = HubDeliveryEntity.fromDomain(anotherDomain);
        HubDelivery domainWithDifferentId = anotherEntity.toDomain();

        // when & then
        assertThatThrownBy(() -> entity.updateFromDomain(domainWithDifferentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("일치하지 않습니다");
    }
}