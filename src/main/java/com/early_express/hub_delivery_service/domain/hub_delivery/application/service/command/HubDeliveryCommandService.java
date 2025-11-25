package com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.command.dto.HubDeliveryCommandDto.*;
import com.early_express.hub_delivery_service.domain.hub_delivery.application.event.HubDeliveryEventPublisher;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryErrorCode;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryException;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryId;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubSegment;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.repository.HubDeliveryRepository;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HubDeliveryCommandService {

    private final HubDeliveryRepository hubDeliveryRepository;
    private final HubDeliveryEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * 허브 배송 생성
     */
    public CreateResult create(CreateCommand command) {
        log.info("허브 배송 생성 - orderId: {}", command.getOrderId());

        // 중복 체크
        if (hubDeliveryRepository.existsByOrderId(command.getOrderId())) {
            throw new HubDeliveryException(
                    HubDeliveryErrorCode.HUB_DELIVERY_ALREADY_EXISTS,
                    "해당 주문의 허브 배송이 이미 존재합니다: " + command.getOrderId()
            );
        }

        // 경로 정보로 HubSegment 생성
        List<HubSegment> segments = createSegments(command.getRouteHubs(), command.getRouteInfoJson());

        // HubDelivery 생성
        HubDelivery hubDelivery = HubDelivery.create(
                command.getOrderId(),
                command.getOriginHubId(),
                command.getDestinationHubId(),
                segments,
                command.getCreatedBy()
        );

        // 저장
        HubDelivery savedHubDelivery = hubDeliveryRepository.save(hubDelivery);

        log.info("허브 배송 생성 완료 - hubDeliveryId: {}, orderId: {}, segments: {}",
                savedHubDelivery.getIdValue(),
                savedHubDelivery.getOrderId(),
                savedHubDelivery.getTotalSegments());

        return CreateResult.success(
                savedHubDelivery.getIdValue(),
                savedHubDelivery.getOrderId(),
                savedHubDelivery.getStatus().name()
        );
    }

    /**
     * 구간 출발 처리
     */
    public void departSegment(DepartSegmentCommand command) {
        log.info("구간 출발 처리 - hubDeliveryId: {}, segment: {}, driverId: {}",
                command.getHubDeliveryId(), command.getSegmentIndex(), command.getDriverId());

        HubDelivery hubDelivery = findHubDelivery(command.getHubDeliveryId());

        // 배송 담당자 배정 (최초 출발 시)
        if (hubDelivery.getDriverId() == null) {
            hubDelivery.assignDriver(command.getDriverId());
        }

        // 구간 출발
        hubDelivery.departSegment(command.getSegmentIndex());

        // 저장
        hubDeliveryRepository.save(hubDelivery);

        // 이벤트 발행
        HubSegment segment = hubDelivery.getSegments().get(command.getSegmentIndex());
        eventPublisher.publishSegmentDeparted(hubDelivery, segment);

        log.info("구간 출발 완료 - hubDeliveryId: {}, segment: {}/{}",
                hubDelivery.getIdValue(),
                command.getSegmentIndex() + 1,
                hubDelivery.getTotalSegments());
    }

    /**
     * 구간 도착 처리
     */
    public void arriveSegment(ArriveSegmentCommand command) {
        log.info("구간 도착 처리 - hubDeliveryId: {}, segment: {}, driverId: {}",
                command.getHubDeliveryId(), command.getSegmentIndex(), command.getDriverId());

        HubDelivery hubDelivery = findHubDelivery(command.getHubDeliveryId());

        // 구간 도착
        hubDelivery.arriveSegment(command.getSegmentIndex());

        // 저장
        hubDeliveryRepository.save(hubDelivery);

        // 이벤트 발행
        HubSegment segment = hubDelivery.getSegments().get(command.getSegmentIndex());
        eventPublisher.publishSegmentArrived(hubDelivery, segment);

        // 전체 완료 시 완료 이벤트 발행
        if (hubDelivery.isCompleted()) {
            eventPublisher.publishHubDeliveryCompleted(hubDelivery);
        }

        log.info("구간 도착 완료 - hubDeliveryId: {}, segment: {}/{}, isCompleted: {}",
                hubDelivery.getIdValue(),
                command.getSegmentIndex() + 1,
                hubDelivery.getTotalSegments(),
                hubDelivery.isCompleted());
    }

    /**
     * 허브 배송 취소 (보상 트랜잭션)
     */
    public CreateResult cancel(CancelCommand command) {
        log.info("허브 배송 취소 - hubDeliveryId: {}", command.getHubDeliveryId());

        HubDelivery hubDelivery = findHubDelivery(command.getHubDeliveryId());

        // 실패 처리
        hubDelivery.fail();

        // 저장
        hubDeliveryRepository.save(hubDelivery);

        log.info("허브 배송 취소 완료 - hubDeliveryId: {}, orderId: {}",
                hubDelivery.getIdValue(), hubDelivery.getOrderId());

        return CreateResult.cancelled(hubDelivery.getIdValue(), hubDelivery.getOrderId());
    }

    // ===== Private Methods =====

    private HubDelivery findHubDelivery(String hubDeliveryId) {
        return hubDeliveryRepository.findById(HubDeliveryId.of(hubDeliveryId))
                .orElseThrow(() -> new HubDeliveryException(
                        HubDeliveryErrorCode.HUB_DELIVERY_NOT_FOUND,
                        "허브 배송 정보를 찾을 수 없습니다: " + hubDeliveryId
                ));
    }

    /**
     * 경로 정보로 HubSegment 리스트 생성
     */
    private List<HubSegment> createSegments(List<String> routeHubs, String routeInfoJson) {
        List<HubSegment> segments = new ArrayList<>();

        // routeInfoJson 파싱
        List<Map<String, Object>> routeInfoList = parseRouteInfo(routeInfoJson);

        for (int i = 0; i < routeHubs.size() - 1; i++) {
            String fromHubId = routeHubs.get(i);
            String toHubId = routeHubs.get(i + 1);

            Long estimatedDistanceM = null;
            Long estimatedDurationMin = null;

            // 경로 상세 정보가 있으면 추출
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
