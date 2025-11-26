package com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryErrorCode;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 허브 구간 값 객체
 *
 * 허브 간 이동의 최소 단위를 나타내며, 불변 객체로 관리됩니다.
 * 상태 변경 시 새로운 인스턴스를 반환합니다.
 *
 * 상태 흐름:
 * PENDING → ASSIGNED → IN_TRANSIT → ARRIVED
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubSegment {

    /** 구간 순서 (0부터 시작) */
    private Integer sequence;

    /** 출발 허브 ID */
    private String fromHubId;

    /** 도착 허브 ID */
    private String toHubId;

    /** 예상 거리 (미터) */
    private Long estimatedDistanceM;

    /** 예상 소요 시간 (분) */
    private Long estimatedDurationMin;

    /** 배정된 드라이버 ID */
    private String driverId;

    /** 구간 상태 */
    private HubSegmentStatus status;

    /** 출발 시각 */
    private LocalDateTime departedAt;

    /** 도착 시각 */
    private LocalDateTime arrivedAt;

    /** 실제 소요 시간 (분) */
    private Long actualDurationMin;

    @Builder
    private HubSegment(Integer sequence, String fromHubId, String toHubId,
                       Long estimatedDistanceM, Long estimatedDurationMin,
                       String driverId, HubSegmentStatus status,
                       LocalDateTime departedAt, LocalDateTime arrivedAt,
                       Long actualDurationMin) {
        this.sequence = sequence;
        this.fromHubId = fromHubId;
        this.toHubId = toHubId;
        this.estimatedDistanceM = estimatedDistanceM;
        this.estimatedDurationMin = estimatedDurationMin;
        this.driverId = driverId;
        this.status = status;
        this.departedAt = departedAt;
        this.arrivedAt = arrivedAt;
        this.actualDurationMin = actualDurationMin;
    }

    // ==================== 팩토리 메서드 ====================

    /**
     * 새 구간 생성
     *
     * @param sequence 구간 순서 (0부터 시작)
     * @param fromHubId 출발 허브 ID
     * @param toHubId 도착 허브 ID
     * @param estimatedDistanceM 예상 거리 (미터)
     * @param estimatedDurationMin 예상 소요 시간 (분)
     * @return 생성된 HubSegment (PENDING 상태)
     */
    public static HubSegment create(Integer sequence, String fromHubId, String toHubId,
                                    Long estimatedDistanceM, Long estimatedDurationMin) {
        return HubSegment.builder()
                .sequence(sequence)
                .fromHubId(fromHubId)
                .toHubId(toHubId)
                .estimatedDistanceM(estimatedDistanceM)
                .estimatedDurationMin(estimatedDurationMin)
                .status(HubSegmentStatus.PENDING)
                .build();
    }

    // ==================== 상태 변경 메서드 (불변) ====================

    /**
     * 드라이버 배정
     *
     * PENDING → ASSIGNED 상태로 전환
     *
     * @param driverId 배정할 드라이버 ID
     * @return 드라이버가 배정된 새 HubSegment
     * @throws HubDeliveryException PENDING 상태가 아닌 경우
     */
    public HubSegment assignDriver(String driverId) {
        if (!this.status.canAssign()) {
            throw new HubDeliveryException(
                    HubDeliveryErrorCode.SEGMENT_CANNOT_ASSIGN,
                    String.format("구간 %d에 드라이버를 배정할 수 없습니다. 현재 상태: %s",
                            this.sequence, this.status.getDescription())
            );
        }

        return HubSegment.builder()
                .sequence(this.sequence)
                .fromHubId(this.fromHubId)
                .toHubId(this.toHubId)
                .estimatedDistanceM(this.estimatedDistanceM)
                .estimatedDurationMin(this.estimatedDurationMin)
                .driverId(driverId)
                .status(HubSegmentStatus.ASSIGNED)
                .departedAt(this.departedAt)
                .arrivedAt(this.arrivedAt)
                .actualDurationMin(this.actualDurationMin)
                .build();
    }

    /**
     * 구간 출발
     *
     * ASSIGNED → IN_TRANSIT 상태로 전환
     * 출발 시각을 현재 시각으로 기록
     *
     * @return 출발 처리된 새 HubSegment
     * @throws HubDeliveryException ASSIGNED 상태가 아닌 경우
     */
    public HubSegment depart() {
        if (!this.status.canDepart()) {
            throw new HubDeliveryException(
                    HubDeliveryErrorCode.SEGMENT_ALREADY_DEPARTED,
                    String.format("구간 %d는 출발할 수 없습니다. 현재 상태: %s",
                            this.sequence, this.status.getDescription())
            );
        }

        return HubSegment.builder()
                .sequence(this.sequence)
                .fromHubId(this.fromHubId)
                .toHubId(this.toHubId)
                .estimatedDistanceM(this.estimatedDistanceM)
                .estimatedDurationMin(this.estimatedDurationMin)
                .driverId(this.driverId)
                .status(HubSegmentStatus.IN_TRANSIT)
                .departedAt(LocalDateTime.now())
                .arrivedAt(this.arrivedAt)
                .actualDurationMin(this.actualDurationMin)
                .build();
    }

    /**
     * 구간 도착
     *
     * IN_TRANSIT → ARRIVED 상태로 전환
     * 도착 시각 기록 및 실제 소요 시간 계산
     *
     * @return 도착 처리된 새 HubSegment
     * @throws HubDeliveryException IN_TRANSIT 상태가 아닌 경우
     */
    public HubSegment arrive() {
        if (!this.status.canArrive()) {
            throw new HubDeliveryException(
                    HubDeliveryErrorCode.SEGMENT_ALREADY_ARRIVED,
                    String.format("구간 %d는 도착 처리할 수 없습니다. 현재 상태: %s",
                            this.sequence, this.status.getDescription())
            );
        }

        LocalDateTime now = LocalDateTime.now();
        Long actualDuration = null;

        if (this.departedAt != null) {
            actualDuration = Duration.between(this.departedAt, now).toMinutes();
        }

        return HubSegment.builder()
                .sequence(this.sequence)
                .fromHubId(this.fromHubId)
                .toHubId(this.toHubId)
                .estimatedDistanceM(this.estimatedDistanceM)
                .estimatedDurationMin(this.estimatedDurationMin)
                .driverId(this.driverId)
                .status(HubSegmentStatus.ARRIVED)
                .departedAt(this.departedAt)
                .arrivedAt(now)
                .actualDurationMin(actualDuration)
                .build();
    }

    /**
     * 구간 실패 처리
     *
     * 현재 상태 → FAILED 상태로 전환
     *
     * @return 실패 처리된 새 HubSegment
     */
    public HubSegment fail() {
        return HubSegment.builder()
                .sequence(this.sequence)
                .fromHubId(this.fromHubId)
                .toHubId(this.toHubId)
                .estimatedDistanceM(this.estimatedDistanceM)
                .estimatedDurationMin(this.estimatedDurationMin)
                .driverId(this.driverId)
                .status(HubSegmentStatus.FAILED)
                .departedAt(this.departedAt)
                .arrivedAt(this.arrivedAt)
                .actualDurationMin(this.actualDurationMin)
                .build();
    }

    // ==================== 상태 조회 메서드 ====================

    /**
     * 대기 중 상태 여부 (드라이버 미배정)
     */
    public boolean isPending() {
        return this.status == HubSegmentStatus.PENDING;
    }

    /**
     * 드라이버 배정 완료 상태 여부
     */
    public boolean isAssigned() {
        return this.status == HubSegmentStatus.ASSIGNED;
    }

    /**
     * 이동 중 상태 여부
     */
    public boolean isInTransit() {
        return this.status == HubSegmentStatus.IN_TRANSIT;
    }

    /**
     * 도착 완료 상태 여부
     */
    public boolean isCompleted() {
        return this.status.isCompleted();
    }

    /**
     * 드라이버 배정 여부
     */
    public boolean hasDriver() {
        return this.driverId != null && !this.driverId.isBlank();
    }
}