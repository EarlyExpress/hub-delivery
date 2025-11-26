package com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.dto.HubDeliveryCommandDto.*;
import com.early_express.hub_delivery_service.domain.hub_delivery.application.event.HubDeliveryEventPublisher;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryErrorCode;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryException;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryId;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.repository.HubDeliveryRepository;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.client.hub_driver.HubDriverClient;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.client.hub_driver.dto.DriverAssignRequest;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.client.hub_driver.dto.DriverAssignResponse;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.client.hub_driver.dto.DriverCompleteRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HubDelivery Command Service
 *
 * 허브 배송의 생성, 상태 변경, 드라이버 배정을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HubDeliveryCommandService {

    private final HubDeliveryRepository hubDeliveryRepository;
    private final HubDeliveryEventPublisher eventPublisher;
    private final HubDriverClient hubDriverClient;
    private final ObjectMapper objectMapper;

    // ==================== 생성 ====================

    /**
     * 허브 배송 생성
     *
     * Order Saga에서 호출됩니다.
     * 드라이버 배정 없이 배송 정보만 생성합니다.
     * 드라이버 배정은 Track Service에서 구간 시작 시 요청합니다.
     *
     * @param command 생성 정보 (orderId, 경로 정보 등)
     * @return 생성 결과
     * @throws HubDeliveryException 중복 주문인 경우
     */
    public CreateResult create(CreateCommand command) {
        log.info("허브 배송 생성 시작 - orderId: {}", command.getOrderId());

        // 1. 중복 체크
        if (hubDeliveryRepository.existsByOrderId(command.getOrderId())) {
            throw new HubDeliveryException(
                    HubDeliveryErrorCode.HUB_DELIVERY_ALREADY_EXISTS,
                    "해당 주문의 허브 배송이 이미 존재합니다: " + command.getOrderId()
            );
        }

        // 2. 경로 정보로 HubSegment 생성 (모두 PENDING 상태)
        List<HubSegment> segments = createSegments(
                command.getRouteHubs(),
                command.getRouteInfoJson()
        );

        // 3. HubDelivery 생성 (드라이버 미배정 상태)
        HubDelivery hubDelivery = HubDelivery.create(
                command.getOrderId(),
                command.getOriginHubId(),
                command.getDestinationHubId(),
                segments,
                command.getCreatedBy()
        );

        // 4. 저장
        HubDelivery savedHubDelivery = hubDeliveryRepository.save(hubDelivery);

        log.info("허브 배송 생성 완료 (드라이버 미배정) - hubDeliveryId: {}, orderId: {}, segments: {}",
                savedHubDelivery.getIdValue(),
                savedHubDelivery.getOrderId(),
                savedHubDelivery.getTotalSegments());

        return CreateResult.success(
                savedHubDelivery.getIdValue(),
                savedHubDelivery.getOrderId(),
                savedHubDelivery.getStatus().name()
        );
    }

    // ==================== 구간 드라이버 배정 ====================

    /**
     * 특정 구간에 드라이버 배정
     *
     * Track Service에서 호출됩니다.
     * 해당 구간의 출발 허브 기준으로 가용 드라이버를 배정합니다.
     * 배정 성공 시 자동으로 구간 출발 처리됩니다.
     *
     * 흐름:
     * 1. HubDriver Service에 드라이버 배정 요청
     * 2. 구간에 드라이버 배정 (PENDING → ASSIGNED)
     * 3. 구간 출발 처리 (ASSIGNED → IN_TRANSIT)
     * 4. SegmentDeparted 이벤트 발행
     *
     * @param command 배정 정보 (hubDeliveryId, segmentIndex)
     * @return 배정 결과
     * @throws HubDeliveryException 배송 정보가 없거나 상태 전환 불가 시
     */
    public AssignDriverResult assignDriverForSegment(AssignDriverForSegmentCommand command) {
        log.info("구간 드라이버 배정 요청 - hubDeliveryId: {}, segment: {}",
                command.getHubDeliveryId(), command.getSegmentIndex());

        // 1. HubDelivery 조회
        HubDelivery hubDelivery = findHubDelivery(command.getHubDeliveryId());

        // 2. 구간 정보 조회
        HubSegment segment = hubDelivery.getSegment(command.getSegmentIndex());

        // 3. 이미 배정된 경우 체크
        if (segment.hasDriver()) {
            log.warn("이미 드라이버가 배정된 구간 - hubDeliveryId: {}, segment: {}, driverId: {}",
                    command.getHubDeliveryId(), command.getSegmentIndex(), segment.getDriverId());
            return AssignDriverResult.failed(
                    command.getHubDeliveryId(),
                    command.getSegmentIndex(),
                    "이미 드라이버가 배정된 구간입니다."
            );
        }

        // 4. HubDriver Service에 드라이버 배정 요청 (출발 허브 기준)
        try {
            DriverAssignResponse response = hubDriverClient.assignDriver(
                    DriverAssignRequest.of(hubDelivery.getIdValue())
            );

            if (!response.isSuccess()) {
                log.warn("드라이버 배정 실패 - hubDeliveryId: {}, segment: {}",
                        command.getHubDeliveryId(), command.getSegmentIndex());
                return AssignDriverResult.failed(
                        command.getHubDeliveryId(),
                        command.getSegmentIndex(),
                        "가용 드라이버가 없습니다."
                );
            }

            // 5. 구간에 드라이버 배정 (PENDING → ASSIGNED)
            hubDelivery.assignDriverToSegment(command.getSegmentIndex(), response.getDriverId());

            // 6. 구간 출발 처리 (ASSIGNED → IN_TRANSIT)
            hubDelivery.departSegment(command.getSegmentIndex());

            // 7. 저장
            hubDeliveryRepository.save(hubDelivery);

            // 8. 출발 이벤트 발행 → Track이 수신
            HubSegment departedSegment = hubDelivery.getSegment(command.getSegmentIndex());
            eventPublisher.publishSegmentDeparted(hubDelivery, departedSegment);

            log.info("구간 드라이버 배정 및 출발 완료 - hubDeliveryId: {}, segment: {}, driverId: {}",
                    hubDelivery.getIdValue(), command.getSegmentIndex(), response.getDriverId());

            return AssignDriverResult.success(
                    hubDelivery.getIdValue(),
                    command.getSegmentIndex(),
                    response.getDriverId(),
                    response.getDriverName()
            );

        } catch (Exception e) {
            log.error("드라이버 배정 중 오류 - hubDeliveryId: {}, segment: {}, error: {}",
                    command.getHubDeliveryId(), command.getSegmentIndex(), e.getMessage(), e);
            return AssignDriverResult.failed(
                    command.getHubDeliveryId(),
                    command.getSegmentIndex(),
                    "드라이버 배정 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }

    // ==================== 구간 상태 변경 ====================

    /**
     * 구간 출발 처리
     *
     * 드라이버 앱에서 직접 호출하는 경우 사용합니다.
     * (Track에서 assignDriverForSegment를 호출하면 자동 출발 처리됨)
     *
     * @param command 출발 정보 (hubDeliveryId, segmentIndex, driverId)
     */
    public void departSegment(DepartSegmentCommand command) {
        log.info("구간 출발 처리 - hubDeliveryId: {}, segment: {}, driverId: {}",
                command.getHubDeliveryId(), command.getSegmentIndex(), command.getDriverId());

        HubDelivery hubDelivery = findHubDelivery(command.getHubDeliveryId());

        // 드라이버 미배정 상태면 배정 처리
        HubSegment segment = hubDelivery.getSegment(command.getSegmentIndex());
        if (!segment.hasDriver() && command.getDriverId() != null) {
            hubDelivery.assignDriverToSegment(command.getSegmentIndex(), command.getDriverId());
        }

        // 구간 출발
        hubDelivery.departSegment(command.getSegmentIndex());

        // 저장
        hubDeliveryRepository.save(hubDelivery);

        // 이벤트 발행
        HubSegment departedSegment = hubDelivery.getSegment(command.getSegmentIndex());
        eventPublisher.publishSegmentDeparted(hubDelivery, departedSegment);

        log.info("구간 출발 완료 - hubDeliveryId: {}, segment: {}/{}",
                hubDelivery.getIdValue(),
                command.getSegmentIndex() + 1,
                hubDelivery.getTotalSegments());
    }

    /**
     * 구간 도착 처리
     *
     * 드라이버 앱에서 호출합니다.
     * 도착 시 드라이버 완료 통지를 전송합니다.
     * 모든 구간 완료 시 HubDeliveryCompleted 이벤트를 발행합니다.
     *
     * @param command 도착 정보 (hubDeliveryId, segmentIndex, driverId)
     */
    public void arriveSegment(ArriveSegmentCommand command) {
        log.info("구간 도착 처리 - hubDeliveryId: {}, segment: {}, driverId: {}",
                command.getHubDeliveryId(), command.getSegmentIndex(), command.getDriverId());

        HubDelivery hubDelivery = findHubDelivery(command.getHubDeliveryId());

        // 구간 도착 처리 (IN_TRANSIT → ARRIVED)
        hubDelivery.arriveSegment(command.getSegmentIndex());

        // 저장
        hubDeliveryRepository.save(hubDelivery);

        // 구간 도착 이벤트 발행 → Track이 수신하여 다음 구간 결정
        HubSegment arrivedSegment = hubDelivery.getSegment(command.getSegmentIndex());
        eventPublisher.publishSegmentArrived(hubDelivery, arrivedSegment);

        // 드라이버 완료 통지
        notifyDriverComplete(arrivedSegment);

        // 모든 구간 완료 시
        if (hubDelivery.isCompleted()) {
            eventPublisher.publishHubDeliveryCompleted(hubDelivery);
            log.info("허브 배송 전체 완료 - hubDeliveryId: {}", hubDelivery.getIdValue());
        }

        log.info("구간 도착 완료 - hubDeliveryId: {}, segment: {}/{}, isCompleted: {}",
                hubDelivery.getIdValue(),
                command.getSegmentIndex() + 1,
                hubDelivery.getTotalSegments(),
                hubDelivery.isCompleted());
    }

    // ==================== 취소 ====================

    /**
     * 허브 배송 취소 (보상 트랜잭션)
     *
     * Order Saga 보상 트랜잭션에서 호출됩니다.
     * 배정된 드라이버에게 취소 통지를 전송합니다.
     *
     * @param command 취소 정보 (hubDeliveryId)
     * @return 취소 결과
     */
    public CreateResult cancel(CancelCommand command) {
        log.info("허브 배송 취소 - hubDeliveryId: {}", command.getHubDeliveryId());

        HubDelivery hubDelivery = findHubDelivery(command.getHubDeliveryId());

        // 진행 중인 구간의 드라이버들에게 취소 통지
        notifyDriversCancel(hubDelivery);

        // 실패 처리
        hubDelivery.fail();

        // 저장
        hubDeliveryRepository.save(hubDelivery);

        log.info("허브 배송 취소 완료 - hubDeliveryId: {}, orderId: {}",
                hubDelivery.getIdValue(), hubDelivery.getOrderId());

        return CreateResult.cancelled(hubDelivery.getIdValue(), hubDelivery.getOrderId());
    }

    // ==================== Private Helper Methods ====================

    /**
     * HubDelivery 조회
     */
    private HubDelivery findHubDelivery(String hubDeliveryId) {
        return hubDeliveryRepository.findById(HubDeliveryId.of(hubDeliveryId))
                .orElseThrow(() -> new HubDeliveryException(
                        HubDeliveryErrorCode.HUB_DELIVERY_NOT_FOUND,
                        "허브 배송 정보를 찾을 수 없습니다: " + hubDeliveryId
                ));
    }

    /**
     * 드라이버 완료 통지
     */
    private void notifyDriverComplete(HubSegment segment) {
        if (segment.getDriverId() == null) {
            return;
        }

        try {
            hubDriverClient.completeDelivery(
                    segment.getDriverId(),
                    DriverCompleteRequest.of(segment.getActualDurationMin())
            );
            log.info("드라이버 완료 통지 성공 - driverId: {}, duration: {}분",
                    segment.getDriverId(), segment.getActualDurationMin());
        } catch (Exception e) {
            log.error("드라이버 완료 통지 실패 - driverId: {}, error: {}",
                    segment.getDriverId(), e.getMessage(), e);
            // 통지 실패해도 배송 처리는 계속
        }
    }

    /**
     * 모든 진행 중인 드라이버에게 취소 통지
     */
    private void notifyDriversCancel(HubDelivery hubDelivery) {
        for (HubSegment segment : hubDelivery.getSegments()) {
            if (segment.hasDriver() && !segment.isCompleted()) {
                try {
                    hubDriverClient.cancelDelivery(segment.getDriverId());
                    log.info("드라이버 취소 통지 성공 - driverId: {}, segment: {}",
                            segment.getDriverId(), segment.getSequence());
                } catch (Exception e) {
                    log.error("드라이버 취소 통지 실패 - driverId: {}, error: {}",
                            segment.getDriverId(), e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 경로 정보로 HubSegment 리스트 생성
     *
     * @param routeHubs 경유 허브 ID 목록
     * @param routeInfoJson 경로 상세 정보 JSON
     * @return 생성된 HubSegment 목록 (모두 PENDING 상태)
     */
    private List<HubSegment> createSegments(List<String> routeHubs, String routeInfoJson) {
        List<HubSegment> segments = new ArrayList<>();
        List<Map<String, Object>> routeInfoList = parseRouteInfo(routeInfoJson);

        for (int i = 0; i < routeHubs.size() - 1; i++) {
            String fromHubId = routeHubs.get(i);
            String toHubId = routeHubs.get(i + 1);

            Long estimatedDistanceM = null;
            Long estimatedDurationMin = null;

            if (routeInfoList != null && i < routeInfoList.size()) {
                Map<String, Object> info = routeInfoList.get(i);
                estimatedDistanceM = getLongValue(info, "distanceM");
                estimatedDurationMin = getLongValue(info, "durationMin");
            }

            HubSegment segment = HubSegment.create(
                    i,
                    fromHubId,
                    toHubId,
                    estimatedDistanceM,
                    estimatedDurationMin
            );

            segments.add(segment);
        }

        return segments;
    }

    private List<Map<String, Object>> parseRouteInfo(String routeInfoJson) {
        if (routeInfoJson == null || routeInfoJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(
                    routeInfoJson,
                    new TypeReference<List<Map<String, Object>>>() {}
            );
        } catch (JsonProcessingException e) {
            log.warn("경로 정보 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
}