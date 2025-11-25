package com.early_express.hub_delivery_service.domain.hub_delivery.domain.model;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryException;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryId;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * HubDelivery Aggregate Root 테스트
 */
class HubDeliveryTest {

    private List<HubSegment> testSegments;

    @BeforeEach
    void setUp() {
        testSegments = List.of(
                HubSegment.create(0, "hub-1", "hub-2", 10000L, 30L),
                HubSegment.create(1, "hub-2", "hub-3", 15000L, 45L)
        );
    }

    @Nested
    @DisplayName("HubDelivery 생성 테스트")
    class CreateTests {

        @Test
        @DisplayName("유효한 데이터로 생성 성공")
        void create_withValidData_shouldSucceed() {
            // when
            HubDelivery hubDelivery = HubDelivery.create(
                    "order-1",
                    "hub-1",
                    "hub-3",
                    testSegments,
                    "system"
            );

            // then
            assertThat(hubDelivery.getOrderId()).isEqualTo("order-1");
            assertThat(hubDelivery.getOriginHubId()).isEqualTo("hub-1");
            assertThat(hubDelivery.getDestinationHubId()).isEqualTo("hub-3");
            assertThat(hubDelivery.getStatus()).isEqualTo(HubDeliveryStatus.CREATED);
            assertThat(hubDelivery.getTotalSegments()).isEqualTo(2);
            assertThat(hubDelivery.getTotalEstimatedDurationMin()).isEqualTo(75L);
        }

        @Test
        @DisplayName("주문 ID 없이 생성 시 예외 발생")
        void create_withoutOrderId_shouldThrowException() {
            // when & then
            assertThatThrownBy(() -> HubDelivery.create(null, "hub-1", "hub-3", testSegments, "system"))
                    .isInstanceOf(HubDeliveryException.class);
        }

        @Test
        @DisplayName("빈 구간으로 생성 시 예외 발생")
        void create_withEmptySegments_shouldThrowException() {
            // when & then
            assertThatThrownBy(() -> HubDelivery.create("order-1", "hub-1", "hub-3", List.of(), "system"))
                    .isInstanceOf(HubDeliveryException.class);
        }
    }

    @Nested
    @DisplayName("배송 담당자 배정 테스트")
    class AssignDriverTests {

        @Test
        @DisplayName("CREATED 상태에서 배송 담당자 배정 성공")
        void assignDriver_fromCreated_shouldSucceed() {
            // given
            HubDelivery hubDelivery = createTestHubDelivery();

            // when
            hubDelivery.assignDriver("driver-1");

            // then
            assertThat(hubDelivery.getDriverId()).isEqualTo("driver-1");
            assertThat(hubDelivery.getStatus()).isEqualTo(HubDeliveryStatus.WAITING_DRIVER);
        }
    }

    @Nested
    @DisplayName("구간 출발/도착 테스트")
    class SegmentTransitionTests {

        @Test
        @DisplayName("첫 번째 구간 출발 성공")
        void departSegment_firstSegment_shouldSucceed() {
            // given
            HubDelivery hubDelivery = createTestHubDelivery();
            hubDelivery.assignDriver("driver-1");

            // when
            hubDelivery.departSegment(0);

            // then
            assertThat(hubDelivery.getStatus()).isEqualTo(HubDeliveryStatus.IN_PROGRESS);
            assertThat(hubDelivery.getCurrentSegmentIndex()).isEqualTo(0);
            assertThat(hubDelivery.getStartedAt()).isNotNull();
            assertThat(hubDelivery.getSegments().get(0).getStatus()).isEqualTo(HubSegmentStatus.IN_TRANSIT);
        }

        @Test
        @DisplayName("구간 도착 성공")
        void arriveSegment_shouldSucceed() {
            // given
            HubDelivery hubDelivery = createTestHubDelivery();
            hubDelivery.assignDriver("driver-1");
            hubDelivery.departSegment(0);

            // when
            hubDelivery.arriveSegment(0);

            // then
            assertThat(hubDelivery.getSegments().get(0).getStatus()).isEqualTo(HubSegmentStatus.ARRIVED);
            assertThat(hubDelivery.getCompletedSegments()).isEqualTo(1);
        }

        @Test
        @DisplayName("모든 구간 완료 시 배송 완료 상태로 전환")
        void arriveSegment_allSegmentsCompleted_shouldCompleteDelivery() {
            // given
            HubDelivery hubDelivery = createTestHubDelivery();
            hubDelivery.assignDriver("driver-1");

            // 첫 번째 구간 완료
            hubDelivery.departSegment(0);
            hubDelivery.arriveSegment(0);

            // 두 번째 구간 완료
            hubDelivery.departSegment(1);
            hubDelivery.arriveSegment(1);

            // then
            assertThat(hubDelivery.getStatus()).isEqualTo(HubDeliveryStatus.COMPLETED);
            assertThat(hubDelivery.isCompleted()).isTrue();
            assertThat(hubDelivery.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("이전 구간 미완료 시 다음 구간 출발 불가")
        void departSegment_previousNotCompleted_shouldThrowException() {
            // given
            HubDelivery hubDelivery = createTestHubDelivery();
            hubDelivery.assignDriver("driver-1");
            hubDelivery.departSegment(0);

            // when & then - 첫 번째 구간이 완료되지 않았는데 두 번째 구간 출발 시도
            assertThatThrownBy(() -> hubDelivery.departSegment(1))
                    .isInstanceOf(HubDeliveryException.class);
        }
    }

    @Nested
    @DisplayName("배송 실패 테스트")
    class FailTests {

        @Test
        @DisplayName("배송 실패 처리 성공")
        void fail_shouldSetStatusToFailed() {
            // given
            HubDelivery hubDelivery = createTestHubDelivery();

            // when
            hubDelivery.fail();

            // then
            assertThat(hubDelivery.getStatus()).isEqualTo(HubDeliveryStatus.FAILED);
            assertThat(hubDelivery.isFailed()).isTrue();
            assertThat(hubDelivery.getCompletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Soft Delete 테스트")
    class DeleteTests {

        @Test
        @DisplayName("삭제 처리 성공")
        void delete_shouldSetIsDeletedTrue() {
            // given
            HubDelivery hubDelivery = createTestHubDelivery();

            // when
            hubDelivery.delete("admin");

            // then
            assertThat(hubDelivery.isDeleted()).isTrue();
            assertThat(hubDelivery.getDeletedAt()).isNotNull();
            assertThat(hubDelivery.getDeletedBy()).isEqualTo("admin");
        }
    }

    private HubDelivery createTestHubDelivery() {
        return HubDelivery.create(
                "order-1",
                "hub-1",
                "hub-3",
                testSegments,
                "system"
        );
    }
}