package com.early_express.hub_delivery_service.domain.hub_delivery.application.service.query;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.event.HubDeliveryEventPublisher;
import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.HubDeliveryCommandService;
import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.dto.HubDeliveryCommandDto.*;
import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.query.dto.HubDeliveryQueryDto.*;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryException;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * HubDeliveryQueryService 통합 테스트
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class HubDeliveryQueryServiceTest {

    @Autowired
    private HubDeliveryQueryService queryService;

    @Autowired
    private HubDeliveryCommandService commandService;

    private String testHubDeliveryId;
    private String testOrderId;

    @BeforeEach
    void setUp() {
        testOrderId = "order-query-" + System.currentTimeMillis();
        CreateCommand createCommand = CreateCommand.builder()
                .orderId(testOrderId)
                .originHubId("hub-1")
                .destinationHubId("hub-3")
                .routeHubs(List.of("hub-1", "hub-2", "hub-3"))
                .createdBy("system")
                .build();

        CreateResult result = commandService.create(createCommand);
        testHubDeliveryId = result.getHubDeliveryId();
    }

    @Test
    @DisplayName("ID로 상세 조회 성공")
    void findById_shouldReturnDetail() {
        // when
        HubDeliveryDetailResponse response = queryService.findById(testHubDeliveryId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getHubDelivery().getHubDeliveryId()).isEqualTo(testHubDeliveryId);
        assertThat(response.getSegments()).hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 예외 발생")
    void findById_notFound_shouldThrowException() {
        // when & then
        assertThatThrownBy(() -> queryService.findById("non-existing-id"))
                .isInstanceOf(HubDeliveryException.class);
    }

    @Test
    @DisplayName("주문 ID로 조회 성공")
    void findByOrderId_shouldReturnDetail() {
        // when
        HubDeliveryDetailResponse response = queryService.findByOrderId(testOrderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getHubDelivery().getOrderId()).isEqualTo(testOrderId);
    }

    @Test
    @DisplayName("전체 목록 조회 성공")
    void findAll_shouldReturnPagedResult() {
        // when
        Page<HubDeliveryResponse> result = queryService.findAll(PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("상태별 목록 조회 성공")
    void findByStatus_shouldReturnFilteredResult() {
        // when
        Page<HubDeliveryResponse> result = queryService.findByStatus(
                HubDeliveryStatus.CREATED,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).allMatch(
                response -> response.getStatus() == HubDeliveryStatus.CREATED
        );
    }
}