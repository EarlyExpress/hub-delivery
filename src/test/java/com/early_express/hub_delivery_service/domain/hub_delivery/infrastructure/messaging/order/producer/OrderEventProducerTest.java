package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.messaging.order.producer;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderEventProducer 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderEventProducer orderEventProducer;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    @Test
    @DisplayName("허브 배송 완료 이벤트 발행 성공")
    void publishHubDeliveryCompleted_shouldSendEvent() {
        // given
        ReflectionTestUtils.setField(orderEventProducer, "hubDeliveryCompletedTopic", "hub-delivery-completed");

        HubDelivery hubDelivery = createCompletedHubDelivery();

        // when
        orderEventProducer.publishHubDeliveryCompleted(hubDelivery);

        // then
        verify(kafkaTemplate, times(1)).send(
                eq("hub-delivery-completed"),
                eq(hubDelivery.getOrderId()),
                eventCaptor.capture()
        );

        Object capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isNotNull();
    }

    private HubDelivery createCompletedHubDelivery() {
        List<HubSegment> segments = List.of(
                HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L)
        );

        HubDelivery hubDelivery = HubDelivery.create(
                "order-1",
                "hub-1",
                "hub-2",
                segments,
                "system"
        );

        hubDelivery.assignDriver("driver-1");
        hubDelivery.departSegment(0);
        hubDelivery.arriveSegment(0);

        return hubDelivery;
    }
}