package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.messaging.track.producer;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import org.junit.jupiter.api.BeforeEach;
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
 * TrackEventProducer 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class TrackEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TrackEventProducer trackEventProducer;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(trackEventProducer, "hubSegmentDepartedTopic", "hub-segment-departed");
        ReflectionTestUtils.setField(trackEventProducer, "hubSegmentArrivedTopic", "hub-segment-arrived");
    }

    @Test
    @DisplayName("허브 구간 출발 이벤트 발행 성공")
    void publishSegmentDeparted_shouldSendEvent() {
        // given
        HubDelivery hubDelivery = createHubDeliveryWithDepartedSegment();
        HubSegment segment = hubDelivery.getSegments().get(0);

        // when
        trackEventProducer.publishSegmentDeparted(hubDelivery, segment);

        // then
        verify(kafkaTemplate, times(1)).send(
                eq("hub-segment-departed"),
                eq(hubDelivery.getOrderId()),
                eventCaptor.capture()
        );

        Object capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isNotNull();
    }

    @Test
    @DisplayName("허브 구간 도착 이벤트 발행 성공")
    void publishSegmentArrived_shouldSendEvent() {
        // given
        HubDelivery hubDelivery = createHubDeliveryWithArrivedSegment();
        HubSegment segment = hubDelivery.getSegments().get(0);

        // when
        trackEventProducer.publishSegmentArrived(hubDelivery, segment);

        // then
        verify(kafkaTemplate, times(1)).send(
                eq("hub-segment-arrived"),
                eq(hubDelivery.getOrderId()),
                eventCaptor.capture()
        );

        Object capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isNotNull();
    }

    private HubDelivery createHubDeliveryWithDepartedSegment() {
        List<HubSegment> segments = List.of(
                HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L),
                HubSegment.create(1, "hub-2", "hub-3", 15000L, 45L)
        );

        HubDelivery hubDelivery = HubDelivery.create(
                "order-1",
                "hub-1",
                "hub-3",
                segments,
                "system"
        );

        hubDelivery.assignDriver("driver-1");
        hubDelivery.departSegment(0);

        return hubDelivery;
    }

    private HubDelivery createHubDeliveryWithArrivedSegment() {
        HubDelivery hubDelivery = createHubDeliveryWithDepartedSegment();
        hubDelivery.arriveSegment(0);
        return hubDelivery;
    }
}