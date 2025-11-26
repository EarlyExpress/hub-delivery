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
 *
 * 허브 간 배송을 관리하는 애그리거트 루트입니다.
 * 여러 개의 HubSegment(구간)으로 구성되며, 각 구간별로 드라이버가 배정됩니다.
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
    private String driverId;  // Deprecated: 구간별 드라이버로 대체
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
     *
     * @param orderId 주문 ID
     * @param originHubId 출발 허브 ID
     * @param destinationHubId 도착 허브 ID
     * @param segments 허브 구간 목록 (모두 PENDING 상태)
     * @param createdBy 생성자
     * @return 생성된 HubDelivery (CREATED 상태)
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
     * 배송 담당자 배정 (전체 배송 레벨)
     *
     * @deprecated 구간별 드라이버 배정을 사용하세요 {@link #assignDriverToSegment(int, String)}
     */
    @Deprecated
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
     * 특정 구간에 드라이버 배정
     *
     * 구간 상태: PENDING → ASSIGNED
     *
     * @param segmentIndex 구간 인덱스 (0부터 시작)
     * @param driverId 드라이버 ID
     * @throws HubDeliveryException 유효하지 않은 구간 인덱스 또는 배정 불가 상태
     */
    public void assignDriverToSegment(int segmentIndex, String driverId) {
        validateNotTerminal();
        validateSegmentIndex(segmentIndex);

        HubSegment segment = this.segments.get(segmentIndex);
        HubSegment assignedSegment = segment.assignDriver(driverId);

        this.segments.set(segmentIndex, assignedSegment);

        log.info("구간 드라이버 배정 - hubDeliveryId: {}, segment: {}/{}, driverId: {}",
                this.getIdValue(),
                segmentIndex + 1,
                this.segments.size(),
                driverId);
    }

    /**
     * 구간 출발
     *
     * 구간 상태: ASSIGNED → IN_TRANSIT
     * 배송 상태: → IN_PROGRESS
     *
     * @param segmentIndex 구간 인덱스
     * @throws HubDeliveryException 이전 구간 미완료 또는 출발 불가 상태
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
     *
     * 구간 상태: IN_TRANSIT → ARRIVED
     * 모든 구간 완료 시 배송 상태: → COMPLETED
     *
     * @param segmentIndex 구간 인덱스
     * @throws HubDeliveryException 도착 불가 상태
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

        // 진행 중인 구간 실패 처리
        for (int i = 0; i < this.segments.size(); i++) {
            HubSegment segment = this.segments.get(i);
            if (!segment.isCompleted() && !segment.getStatus().isTerminal()) {
                this.segments.set(i, segment.fail());
            }
        }

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

    /**
     * 특정 구간 조회
     *
     * @param segmentIndex 구간 인덱스 (0부터 시작)
     * @return 해당 구간
     * @throws HubDeliveryException 유효하지 않은 구간 인덱스
     */
    public HubSegment getSegment(int segmentIndex) {
        validateSegmentIndex(segmentIndex);
        return this.segments.get(segmentIndex);
    }

    /**
     * 총 구간 수
     */
    public int getTotalSegments() {
        return this.segments.size();
    }

    /**
     * 완료된 구간 수
     */
    public int getCompletedSegments() {
        return (int) this.segments.stream()
                .filter(HubSegment::isCompleted)
                .count();
    }

    /**
     * 현재 진행 중인 구간 조회
     *
     * @return 현재 구간 (없으면 null)
     */
    public HubSegment getCurrentSegment() {
        if (this.currentSegmentIndex != null && this.currentSegmentIndex < this.segments.size()) {
            return this.segments.get(this.currentSegmentIndex);
        }
        return null;
    }

    /**
     * 다음 대기 중인 구간 조회
     *
     * @return 다음 PENDING 상태 구간 (없으면 null)
     */
    public HubSegment getNextPendingSegment() {
        return this.segments.stream()
                .filter(HubSegment::isPending)
                .findFirst()
                .orElse(null);
    }

    /**
     * 다음 대기 중인 구간 인덱스 조회
     *
     * @return 다음 PENDING 상태 구간 인덱스 (없으면 -1)
     */
    public int getNextPendingSegmentIndex() {
        for (int i = 0; i < this.segments.size(); i++) {
            if (this.segments.get(i).isPending()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 모든 구간 완료 여부
     */
    public boolean isAllSegmentsCompleted() {
        return this.segments.stream().allMatch(HubSegment::isCompleted);
    }

    /**
     * 다음 구간 존재 여부
     */
    public boolean hasNextSegment() {
        return getNextPendingSegmentIndex() >= 0;
    }

    /**
     * 배송 완료 여부
     */
    public boolean isCompleted() {
        return this.status == HubDeliveryStatus.COMPLETED;
    }

    /**
     * 배송 실패 여부
     */
    public boolean isFailed() {
        return this.status == HubDeliveryStatus.FAILED;
    }

    /**
     * 배송 진행 중 여부
     */
    public boolean isInProgress() {
        return this.status.isInProgress();
    }

    /**
     * 각 구간별 ID 목록 반환
     *
     * @return 구간 ID 목록 (hubDeliveryId-segment-0, hubDeliveryId-segment-1, ...)
     */
    public List<String> getSegmentIds() {
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