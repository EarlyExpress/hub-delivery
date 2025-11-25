package com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryErrorCode;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 허브 구간 값 객체
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubSegment {

    private Integer sequence;           // 구간 순서 (0부터 시작)
    private String fromHubId;           // 출발 허브 ID
    private String toHubId;             // 도착 허브 ID
    private Long estimatedDistanceM;    // 예상 거리 (미터)
    private Long estimatedDurationMin;  // 예상 소요 시간 (분)
    private HubSegmentStatus status;
    private LocalDateTime departedAt;
    private LocalDateTime arrivedAt;
    private Long actualDurationMin;     // 실제 소요 시간 (분)

    @Builder
    private HubSegment(Integer sequence, String fromHubId, String toHubId,
                       Long estimatedDistanceM, Long estimatedDurationMin,
                       HubSegmentStatus status, LocalDateTime departedAt,
                       LocalDateTime arrivedAt, Long actualDurationMin) {
        this.sequence = sequence;
        this.fromHubId = fromHubId;
        this.toHubId = toHubId;
        this.estimatedDistanceM = estimatedDistanceM;
        this.estimatedDurationMin = estimatedDurationMin;
        this.status = status;
        this.departedAt = departedAt;
        this.arrivedAt = arrivedAt;
        this.actualDurationMin = actualDurationMin;
    }

    /**
     * 새 구간 생성
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

    /**
     * 구간 출발
     */
    public HubSegment depart() {
        if (!this.status.canDepart()) {
            throw new HubDeliveryException(
                    HubDeliveryErrorCode.SEGMENT_ALREADY_DEPARTED,
                    String.format("구간 %d는 이미 출발했습니다. 현재 상태: %s",
                            this.sequence, this.status.getDescription())
            );
        }

        return HubSegment.builder()
                .sequence(this.sequence)
                .fromHubId(this.fromHubId)
                .toHubId(this.toHubId)
                .estimatedDistanceM(this.estimatedDistanceM)
                .estimatedDurationMin(this.estimatedDurationMin)
                .status(HubSegmentStatus.IN_TRANSIT)
                .departedAt(LocalDateTime.now())
                .arrivedAt(this.arrivedAt)
                .actualDurationMin(this.actualDurationMin)
                .build();
    }

    /**
     * 구간 도착
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
            actualDuration = java.time.Duration.between(this.departedAt, now).toMinutes();
        }

        return HubSegment.builder()
                .sequence(this.sequence)
                .fromHubId(this.fromHubId)
                .toHubId(this.toHubId)
                .estimatedDistanceM(this.estimatedDistanceM)
                .estimatedDurationMin(this.estimatedDurationMin)
                .status(HubSegmentStatus.ARRIVED)
                .departedAt(this.departedAt)
                .arrivedAt(now)
                .actualDurationMin(actualDuration)
                .build();
    }

    /**
     * 구간 실패
     */
    public HubSegment fail() {
        return HubSegment.builder()
                .sequence(this.sequence)
                .fromHubId(this.fromHubId)
                .toHubId(this.toHubId)
                .estimatedDistanceM(this.estimatedDistanceM)
                .estimatedDurationMin(this.estimatedDurationMin)
                .status(HubSegmentStatus.FAILED)
                .departedAt(this.departedAt)
                .arrivedAt(this.arrivedAt)
                .actualDurationMin(this.actualDurationMin)
                .build();
    }

    public boolean isPending() {
        return this.status == HubSegmentStatus.PENDING;
    }

    public boolean isInTransit() {
        return this.status == HubSegmentStatus.IN_TRANSIT;
    }

    public boolean isCompleted() {
        return this.status.isCompleted();
    }
}
