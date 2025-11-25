package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.persistence.entity;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryId;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import com.early_express.hub_delivery_service.global.common.utils.UuidUtils;
import com.early_express.hub_delivery_service.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * HubDelivery JPA Entity
 */
@Entity
@Table(name = "p_hub_delivery", indexes = {
        @Index(name = "idx_hub_delivery_order_id", columnList = "order_id"),
        @Index(name = "idx_hub_delivery_status", columnList = "status"),
        @Index(name = "idx_hub_delivery_driver_id", columnList = "driver_id"),
        @Index(name = "idx_hub_delivery_origin_hub", columnList = "origin_hub_id"),
        @Index(name = "idx_hub_delivery_destination_hub", columnList = "destination_hub_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubDeliveryEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "origin_hub_id", nullable = false, length = 36)
    private String originHubId;

    @Column(name = "destination_hub_id", nullable = false, length = 36)
    private String destinationHubId;

    @ElementCollection
    @CollectionTable(
            name = "p_hub_delivery_segment",
            joinColumns = @JoinColumn(name = "hub_delivery_id")
    )
    @OrderBy("sequence ASC")
    private List<HubSegmentEmbeddable> segments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private HubDeliveryStatus status;

    @Column(name = "driver_id", length = 36)
    private String driverId;

    @Column(name = "current_segment_index")
    private Integer currentSegmentIndex;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "total_estimated_duration_min")
    private Long totalEstimatedDurationMin;

    @Column(name = "total_actual_duration_min")
    private Long totalActualDurationMin;

    @Builder
    private HubDeliveryEntity(String id, String orderId, String originHubId,
                              String destinationHubId, List<HubSegmentEmbeddable> segments,
                              HubDeliveryStatus status, String driverId,
                              Integer currentSegmentIndex, LocalDateTime startedAt,
                              LocalDateTime completedAt, Long totalEstimatedDurationMin,
                              Long totalActualDurationMin) {
        this.id = id;
        this.orderId = orderId;
        this.originHubId = originHubId;
        this.destinationHubId = destinationHubId;
        this.segments = segments != null ? new ArrayList<>(segments) : new ArrayList<>();
        this.status = status;
        this.driverId = driverId;
        this.currentSegmentIndex = currentSegmentIndex;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.totalEstimatedDurationMin = totalEstimatedDurationMin;
        this.totalActualDurationMin = totalActualDurationMin;
    }

    // ===== 도메인 → 엔티티 변환 =====

    public static HubDeliveryEntity fromDomain(HubDelivery hubDelivery) {
        String entityId = hubDelivery.getIdValue() != null
                ? hubDelivery.getIdValue()
                : UuidUtils.generate();

        List<HubSegmentEmbeddable> segmentEmbeddables = hubDelivery.getSegments().stream()
                .map(HubSegmentEmbeddable::from)
                .toList();

        return HubDeliveryEntity.builder()
                .id(entityId)
                .orderId(hubDelivery.getOrderId())
                .originHubId(hubDelivery.getOriginHubId())
                .destinationHubId(hubDelivery.getDestinationHubId())
                .segments(segmentEmbeddables)
                .status(hubDelivery.getStatus())
                .driverId(hubDelivery.getDriverId())
                .currentSegmentIndex(hubDelivery.getCurrentSegmentIndex())
                .startedAt(hubDelivery.getStartedAt())
                .completedAt(hubDelivery.getCompletedAt())
                .totalEstimatedDurationMin(hubDelivery.getTotalEstimatedDurationMin())
                .totalActualDurationMin(hubDelivery.getTotalActualDurationMin())
                .build();
    }

    // ===== 엔티티 → 도메인 변환 =====

    public HubDelivery toDomain() {
        List<HubSegment> domainSegments = this.segments.stream()
                .map(HubSegmentEmbeddable::toDomain)
                .toList();

        return HubDelivery.reconstitute(
                HubDeliveryId.of(this.id),
                this.orderId,
                this.originHubId,
                this.destinationHubId,
                domainSegments,
                this.status,
                this.driverId,
                this.currentSegmentIndex,
                this.startedAt,
                this.completedAt,
                this.totalEstimatedDurationMin,
                this.totalActualDurationMin,
                this.getCreatedAt(),
                this.getCreatedBy(),
                this.getUpdatedAt(),
                this.getUpdatedBy(),
                this.getDeletedAt(),
                this.getDeletedBy(),
                this.isDeleted()
        );
    }

    // ===== 도메인 → 엔티티 업데이트 =====

    public void updateFromDomain(HubDelivery hubDelivery) {
        if (!this.id.equals(hubDelivery.getIdValue())) {
            throw new IllegalStateException(
                    "엔티티 ID와 도메인 ID가 일치하지 않습니다."
            );
        }

        // segments 업데이트
        this.segments.clear();
        this.segments.addAll(
                hubDelivery.getSegments().stream()
                        .map(HubSegmentEmbeddable::from)
                        .toList()
        );

        this.status = hubDelivery.getStatus();
        this.driverId = hubDelivery.getDriverId();
        this.currentSegmentIndex = hubDelivery.getCurrentSegmentIndex();
        this.startedAt = hubDelivery.getStartedAt();
        this.completedAt = hubDelivery.getCompletedAt();
        this.totalActualDurationMin = hubDelivery.getTotalActualDurationMin();
    }
}
