package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.persistence.repository;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryId;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.repository.HubDeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * HubDeliveryRepository 통합 테스트
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class HubDeliveryRepositoryImplTest {

    @Autowired
    private HubDeliveryRepository hubDeliveryRepository;

    private HubDelivery testHubDelivery;

    @BeforeEach
    void setUp() {
        List<HubSegment> segments = List.of(
                HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L),
                HubSegment.create(1, "hub-2", "hub-3", 15000L, 45L)
        );

        testHubDelivery = HubDelivery.create(
                "order-test-" + System.currentTimeMillis(),
                "hub-1",
                "hub-3",
                segments,
                "system"
        );
    }

    @Test
    @DisplayName("HubDelivery 저장 성공")
    void save_newHubDelivery_shouldSucceed() {
        // when
        HubDelivery savedHubDelivery = hubDeliveryRepository.save(testHubDelivery);

        // then
        assertThat(savedHubDelivery.getIdValue()).isNotNull();
        assertThat(savedHubDelivery.getOrderId()).isEqualTo(testHubDelivery.getOrderId());
        assertThat(savedHubDelivery.getStatus()).isEqualTo(HubDeliveryStatus.CREATED);
    }

    @Test
    @DisplayName("ID로 HubDelivery 조회 성공")
    void findById_existingId_shouldReturnHubDelivery() {
        // given
        HubDelivery savedHubDelivery = hubDeliveryRepository.save(testHubDelivery);
        HubDeliveryId id = HubDeliveryId.of(savedHubDelivery.getIdValue());

        // when
        Optional<HubDelivery> found = hubDeliveryRepository.findById(id);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getIdValue()).isEqualTo(savedHubDelivery.getIdValue());
    }

    @Test
    @DisplayName("주문 ID로 HubDelivery 조회 성공")
    void findByOrderId_existingOrderId_shouldReturnHubDelivery() {
        // given
        HubDelivery savedHubDelivery = hubDeliveryRepository.save(testHubDelivery);

        // when
        Optional<HubDelivery> found = hubDeliveryRepository.findByOrderId(savedHubDelivery.getOrderId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo(savedHubDelivery.getOrderId());
    }

    @Test
    @DisplayName("전체 목록 조회 성공")
    void findAll_shouldReturnPagedResult() {
        // given
        hubDeliveryRepository.save(testHubDelivery);

        // when
        Page<HubDelivery> result = hubDeliveryRepository.findAll(PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("상태별 목록 조회 성공")
    void findByStatus_shouldReturnFilteredResult() {
        // given
        hubDeliveryRepository.save(testHubDelivery);

        // when
        Page<HubDelivery> result = hubDeliveryRepository.findByStatus(
                HubDeliveryStatus.CREATED,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).allMatch(
                hd -> hd.getStatus() == HubDeliveryStatus.CREATED
        );
    }

    @Test
    @DisplayName("주문 ID 존재 여부 확인 성공")
    void existsByOrderId_existingOrderId_shouldReturnTrue() {
        // given
        HubDelivery savedHubDelivery = hubDeliveryRepository.save(testHubDelivery);

        // when
        boolean exists = hubDeliveryRepository.existsByOrderId(savedHubDelivery.getOrderId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID 확인 시 false 반환")
    void existsByOrderId_nonExistingOrderId_shouldReturnFalse() {
        // when
        boolean exists = hubDeliveryRepository.existsByOrderId("non-existing-order-id");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("HubDelivery 업데이트 성공")
    void save_existingHubDelivery_shouldUpdate() {
        // given
        HubDelivery savedHubDelivery = hubDeliveryRepository.save(testHubDelivery);
        savedHubDelivery.assignDriver("driver-1");

        // when
        HubDelivery updatedHubDelivery = hubDeliveryRepository.save(savedHubDelivery);

        // then
        assertThat(updatedHubDelivery.getDriverId()).isEqualTo("driver-1");
        assertThat(updatedHubDelivery.getStatus()).isEqualTo(HubDeliveryStatus.WAITING_DRIVER);
    }
}