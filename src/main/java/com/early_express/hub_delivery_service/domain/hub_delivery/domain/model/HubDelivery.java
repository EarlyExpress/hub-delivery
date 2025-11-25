package com.early_express.hub_delivery_service.domain.hub_delivery.domain.model;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryErrorCode;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryException;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Hub Delivery Aggregate Root
 */
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubDelivery {

    private HubDeliveryId id;
    private String orderId;
    private String originHubId;
    private String destinationHubId;
    private List<HubSegment> segments;
    private HubDeliveryStatus status;
    private String driverId;
    private Integer currentSegmentIndex;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long totalEstimatedDurationMin;
    private Long totalActualDurationMin;

    // Audit 필드
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private boolean isDeleted;

    @Builder
    private HubDelivery(HubDeliveryId id, String orderId, String originHubId,
                        String destinationHubId, List<HubSegment> segments,
                        HubDeliveryStatus status, String driverId,
                        Integer currentSegmentIndex, LocalDateTime startedAt,
                        LocalDateTime completedAt, Long totalEstimatedDurationMin,
                        Long totalActualDurationMin, LocalDateTime createdAt,
                        String createdBy, LocalDateTime updatedAt, String updatedBy,
                        LocalDateTime deletedAt, String deletedBy, boolean isDeleted) {
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
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
        this.isDeleted = isDeleted;
    }

    // ===== 팩토리 메서드 =====

    /**
     * 새로운 HubDelivery 생성
     */
    public static HubDelivery create(String orderId, String originHubId,
                                     String destinationHubId, List<HubSegment> segments,
                                     String createdBy) {
        validateNotBlank(orderId, "주문 ID");
        validateNotBlank(originHubId, "출발 허브 ID");
        validateNotBlank(destinationHubId, "도착 허브 ID");
        validateNotEmpty(segments, "허브 구간");

        Long totalEstimatedDuration = segments.stream()
                .mapToLong(s -> s.getEstimatedDurationMin() != null ? s.getEstimatedDurationMin() : 0)
                .sum();

        return HubDelivery.builder()
                .id(null)  // Entity에서 UUID 생성
                .orderId(orderId)
                .originHubId(originHubId)
                .destinationHubId(destinationHubId)
                .segments(segments)
                .status(HubDeliveryStatus.CREATED)
                .currentSegmentIndex(0)
                .totalEstimatedDurationMin(totalEstimatedDuration)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * DB 조회 후 도메인 복원용
     */
    public static HubDelivery reconstitute(
            HubDeliveryId id, String orderId, String originHubId,
            String destinationHubId, List<HubSegment> segments,
            HubDeliveryStatus status, String driverId,
            Integer currentSegmentIndex, LocalDateTime startedAt,
            LocalDateTime completedAt, Long totalEstimatedDurationMin,
            Long totalActualDurationMin, LocalDateTime createdAt,
            String createdBy, LocalDateTime updatedAt, String updatedBy,
            LocalDateTime deletedAt, String deletedBy, boolean isDeleted) {

        return HubDelivery.builder()
                .id(id)
                .orderId(orderId)
                .originHubId(originHubId)
                .destinationHubId(destinationHubId)
                .segments(segments)
                .status(status)
                .driverId(driverId)
                .currentSegmentIndex(currentSegmentIndex)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .totalEstimatedDurationMin(totalEstimatedDurationMin)
                .totalActualDurationMin(totalActualDurationMin)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .deletedAt(deletedAt)
                .deletedBy(deletedBy)
                .isDeleted(isDeleted)
                .build();
    }

    // ===== 비즈니스 메서드 =====

    /**
     * 배송 담당자 배정
     */
    public void assignDriver(String driverId) {
        validateNotTerminal();

        if (!this.status.canAssignDriver()) {
            throw new HubDeliveryException(
                    HubDeliveryErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("배송 담당자 배정은 CREATED 또는 WAITING_DRIVER 상태에서만 가능합니다. 현재 상태: %s",
                            this.status.getDescription())
            );
        }

        this.driverId = driverId;
        this.status = HubDeliveryStatus.WAITING_DRIVER;

        log.info("배송 담당자 배정 - hubDeliveryId: {}, driverId: {}",
                this.getIdValue(), driverId);
    }

    /**
     * 구간 출발
     */
    public void departSegment(int segmentIndex) {
        validateNotTerminal();
        validateSegmentIndex(segmentIndex);
        validatePreviousSegmentCompleted(segmentIndex);

        HubSegment segment = this.segments.get(segmentIndex);
        HubSegment departedSegment = segment.depart();

        this.segments.set(segmentIndex, departedSegment);
        this.currentSegmentIndex = segmentIndex;
        this.status = HubDeliveryStatus.IN_PROGRESS;

        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }

        log.info("구간 출발 - hubDeliveryId: {}, segment: {}/{}, from: {} → to: {}",
                this.getIdValue(),
                segmentIndex + 1,
                this.segments.size(),
                departedSegment.getFromHubId(),
                departedSegment.getToHubId());
    }

    /**
     * 구간 도착
     */
    public void arriveSegment(int segmentIndex) {
        validateNotTerminal();
        validateSegmentIndex(segmentIndex);

        HubSegment segment = this.segments.get(segmentIndex);
        HubSegment arrivedSegment = segment.arrive();

        this.segments.set(segmentIndex, arrivedSegment);

        log.info("구간 도착 - hubDeliveryId: {}, segment: {}/{}",
                this.getIdValue(),
                segmentIndex + 1,
                this.segments.size());

        // 모든 구간 완료 시 배송 완료
        if (isAllSegmentsCompleted()) {
            complete();
        }
    }

    /**
     * 배송 완료
     */
    public void complete() {
        this.status = HubDeliveryStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();

        // 총 실제 소요 시간 계산
        if (this.startedAt != null) {
            this.totalActualDurationMin = java.time.Duration
                    .between(this.startedAt, this.completedAt).toMinutes();
        }

        log.info("허브 배송 완료 - hubDeliveryId: {}, orderId: {}, 소요시간: {}분",
                this.getIdValue(), this.orderId, this.totalActualDurationMin);
    }

    /**
     * 배송 실패
     */
    public void fail() {
        this.status = HubDeliveryStatus.FAILED;
        this.completedAt = LocalDateTime.now();

        log.info("허브 배송 실패 - hubDeliveryId: {}, orderId: {}",
                this.getIdValue(), this.orderId);
    }

    // ===== Soft Delete =====

    public void delete(String deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    // ===== 검증 메서드 =====

    private void validateNotTerminal() {
        if (this.status.isTerminal()) {
            if (this.status == HubDeliveryStatus.COMPLETED) {
                throw new HubDeliveryException(HubDeliveryErrorCode.HUB_DELIVERY_ALREADY_COMPLETED);
            } else {
                throw new HubDeliveryException(HubDeliveryErrorCode.HUB_DELIVERY_ALREADY_FAILED);
            }
        }
    }

    private void validateSegmentIndex(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= this.segments.size()) {
            throw new HubDeliveryException(
                    HubDeliveryErrorCode.INVALID_SEGMENT_INDEX,
                    String.format("유효하지 않은 구간 순서입니다. index: %d, total: %d",
                            segmentIndex, this.segments.size())
            );
        }
    }

    private void validatePreviousSegmentCompleted(int segmentIndex) {
        if (segmentIndex > 0) {
            HubSegment previousSegment = this.segments.get(segmentIndex - 1);
            if (!previousSegment.isCompleted()) {
                throw new HubDeliveryException(
                        HubDeliveryErrorCode.SEGMENT_NOT_READY,
                        String.format("이전 구간 %d가 완료되지 않았습니다.", segmentIndex)
                );
            }
        }
    }

    private static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new HubDeliveryException(
                    HubDeliveryErrorCode.INVALID_ORDER_ID,
                    fieldName + "는 필수입니다."
            );
        }
    }

    private static void validateNotEmpty(List<?> list, String fieldName) {
        if (list == null || list.isEmpty()) {
            throw new HubDeliveryException(
                    HubDeliveryErrorCode.EMPTY_SEGMENTS,
                    fieldName + "는 비어있을 수 없습니다."
            );
        }
    }

    // ===== 조회 메서드 =====

    public String getIdValue() {
        return this.id != null ? this.id.getValue() : null;
    }

    public int getTotalSegments() {
        return this.segments.size();
    }

    public int getCompletedSegments() {
        return (int) this.segments.stream()
                .filter(HubSegment::isCompleted)
                .count();
    }

    public HubSegment getCurrentSegment() {
        if (this.currentSegmentIndex != null && this.currentSegmentIndex < this.segments.size()) {
            return this.segments.get(this.currentSegmentIndex);
        }
        return null;
    }

    public boolean isAllSegmentsCompleted() {
        return this.segments.stream().allMatch(HubSegment::isCompleted);
    }

    public boolean isCompleted() {
        return this.status == HubDeliveryStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == HubDeliveryStatus.FAILED;
    }

    public boolean isInProgress() {
        return this.status.isInProgress();
    }

    public List<String> getSegmentIds() {
        // 각 구간별 ID 반환 (hubDeliveryId-segment-0, hubDeliveryId-segment-1, ...)
        List<String> ids = new ArrayList<>();
        String baseId = this.getIdValue();
        if (baseId != null) {
            for (int i = 0; i < this.segments.size(); i++) {
                ids.add(baseId + "-segment-" + i);
            }
        }
        return ids;
    }
}
