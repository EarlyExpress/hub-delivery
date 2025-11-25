package com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.event.HubDeliveryEventPublisher;
import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.dto.HubDeliveryCommandDto.*;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryException;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryId;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.repository.HubDeliveryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HubDeliveryCommandService 통합 테스트
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class HubDeliveryCommandServiceTest {

    @Autowired
    private HubDeliveryCommandService commandService;

    @Autowired
    private HubDeliveryRepository hubDeliveryRepository;

    // Kafka 이벤트 발행을 Mock 처리
    @MockBean
    private HubDeliveryEventPublisher eventPublisher;

    @Test
    @DisplayName("허브 배송 생성 성공")
    void create_shouldCreateHubDelivery() {
        // given
        CreateCommand command = CreateCommand.builder()
                .orderId("order-test-" + System.currentTimeMillis())
                .originHubId("hub-1")
                .destinationHubId("hub-3")
                .routeHubs(List.of("hub-1", "hub-2", "hub-3"))
                .createdBy("system")
                .build();

        // when
        CreateResult result = commandService.create(command);

        // then
        assertThat(result.getHubDeliveryId()).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(command.getOrderId());
        assertThat(result.getStatus()).isEqualTo("CREATED");
    }

    @Test
    @DisplayName("중복 주문 ID로 생성 시 예외 발생")
    void create_duplicateOrderId_shouldThrowException() {
        // given
        String orderId = "order-duplicate-" + System.currentTimeMillis();
        CreateCommand command = CreateCommand.builder()
                .orderId(orderId)
                .originHubId("hub-1")
                .destinationHubId("hub-3")
                .routeHubs(List.of("hub-1", "hub-2", "hub-3"))
                .createdBy("system")
                .build();

        commandService.create(command);

        // when & then
        assertThatThrownBy(() -> commandService.create(command))
                .isInstanceOf(HubDeliveryException.class);
    }

    @Test
    @DisplayName("구간 출발 처리 성공")
    void departSegment_shouldSucceed() {
        // given
        CreateCommand createCommand = CreateCommand.builder()
                .orderId("order-depart-" + System.currentTimeMillis())
                .originHubId("hub-1")
                .destinationHubId("hub-3")
                .routeHubs(List.of("hub-1", "hub-2", "hub-3"))
                .createdBy("system")
                .build();
        CreateResult createResult = commandService.create(createCommand);

        DepartSegmentCommand departCommand = DepartSegmentCommand.builder()
                .hubDeliveryId(createResult.getHubDeliveryId())
                .segmentIndex(0)
                .driverId("driver-1")
                .build();

        // when
        commandService.departSegment(departCommand);

        // then
        HubDelivery hubDelivery = hubDeliveryRepository
                .findById(HubDeliveryId.of(createResult.getHubDeliveryId()))
                .orElseThrow();

        assertThat(hubDelivery.getStatus()).isEqualTo(HubDeliveryStatus.IN_PROGRESS);
        assertThat(hubDelivery.getDriverId()).isEqualTo("driver-1");

        // 이벤트 발행 검증
        verify(eventPublisher, times(1)).publishSegmentDeparted(any(), any());
    }

    @Test
    @DisplayName("구간 도착 처리 성공")
    void arriveSegment_shouldSucceed() {
        // given
        CreateCommand createCommand = CreateCommand.builder()
                .orderId("order-arrive-" + System.currentTimeMillis())
                .originHubId("hub-1")
                .destinationHubId("hub-3")
                .routeHubs(List.of("hub-1", "hub-2", "hub-3"))
                .createdBy("system")
                .build();
        CreateResult createResult = commandService.create(createCommand);

        // 먼저 출발
        commandService.departSegment(DepartSegmentCommand.builder()
                .hubDeliveryId(createResult.getHubDeliveryId())
                .segmentIndex(0)
                .driverId("driver-1")
                .build());

        ArriveSegmentCommand arriveCommand = ArriveSegmentCommand.builder()
                .hubDeliveryId(createResult.getHubDeliveryId())
                .segmentIndex(0)
                .driverId("driver-1")
                .build();

        // when
        commandService.arriveSegment(arriveCommand);

        // then
        HubDelivery hubDelivery = hubDeliveryRepository
                .findById(HubDeliveryId.of(createResult.getHubDeliveryId()))
                .orElseThrow();

        assertThat(hubDelivery.getCompletedSegments()).isEqualTo(1);

        // 이벤트 발행 검증
        verify(eventPublisher, times(1)).publishSegmentArrived(any(), any());
    }

    @Test
    @DisplayName("허브 배송 취소 성공")
    void cancel_shouldSucceed() {
        // given
        CreateCommand createCommand = CreateCommand.builder()
                .orderId("order-cancel-" + System.currentTimeMillis())
                .originHubId("hub-1")
                .destinationHubId("hub-3")
                .routeHubs(List.of("hub-1", "hub-2", "hub-3"))
                .createdBy("system")
                .build();
        CreateResult createResult = commandService.create(createCommand);

        CancelCommand cancelCommand = CancelCommand.builder()
                .hubDeliveryId(createResult.getHubDeliveryId())
                .cancelledBy("admin")
                .build();

        // when
        CreateResult cancelResult = commandService.cancel(cancelCommand);

        // then
        assertThat(cancelResult.getStatus()).isEqualTo("CANCELLED");

        HubDelivery hubDelivery = hubDeliveryRepository
                .findById(HubDeliveryId.of(createResult.getHubDeliveryId()))
                .orElseThrow();
        assertThat(hubDelivery.getStatus()).isEqualTo(HubDeliveryStatus.FAILED);
    }
}