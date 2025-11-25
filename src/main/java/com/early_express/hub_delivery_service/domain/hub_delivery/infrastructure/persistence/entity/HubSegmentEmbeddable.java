package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.persistence.entity;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * HubSegment Embeddable
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubSegmentEmbeddable {

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "from_hub_id", nullable = false, length = 36)
    private String fromHubId;

    @Column(name = "to_hub_id", nullable = false, length = 36)
    private String toHubId;

    @Column(name = "estimated_distance_m")
    private Long estimatedDistanceM;

    @Column(name = "estimated_duration_min")
    private Long estimatedDurationMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HubSegmentStatus status;

    @Column(name = "departed_at")
    private LocalDateTime departedAt;

    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Column(name = "actual_duration_min")
    private Long actualDurationMin;

    @Builder
    private HubSegmentEmbeddable(Integer sequence, String fromHubId, String toHubId,
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
     * 도메인 → Embeddable 변환
     */
    public static HubSegmentEmbeddable from(HubSegment segment) {
        return HubSegmentEmbeddable.builder()
                .sequence(segment.getSequence())
                .fromHubId(segment.getFromHubId())
                .toHubId(segment.getToHubId())
                .estimatedDistanceM(segment.getEstimatedDistanceM())
                .estimatedDurationMin(segment.getEstimatedDurationMin())
                .status(segment.getStatus())
                .departedAt(segment.getDepartedAt())
                .arrivedAt(segment.getArrivedAt())
                .actualDurationMin(segment.getActualDurationMin())
                .build();
    }

    /**
     * Embeddable → 도메인 변환
     */
    public HubSegment toDomain() {
        return HubSegment.builder()
                .sequence(this.sequence)
                .fromHubId(this.fromHubId)
                .toHubId(this.toHubId)
                .estimatedDistanceM(this.estimatedDistanceM)
                .estimatedDurationMin(this.estimatedDurationMin)
                .status(this.status)
                .departedAt(this.departedAt)
                .arrivedAt(this.arrivedAt)
                .actualDurationMin(this.actualDurationMin)
                .build();
    }
}
