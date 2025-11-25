package com.early_express.hub_delivery_service.domain.hub_delivery.application.service.query;

import com.early_express.hub_delivery_service.domain.hub_delivery.application.service.query.dto.HubDeliveryQueryDto.*;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryErrorCode;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryException;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryId;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.repository.HubDeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HubDelivery Query Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubDeliveryQueryService {

    private final HubDeliveryRepository hubDeliveryRepository;

    /**
     * ID로 상세 조회
     */
    public HubDeliveryDetailResponse findById(String hubDeliveryId) {
        HubDelivery hubDelivery = hubDeliveryRepository.findById(HubDeliveryId.of(hubDeliveryId))
                .orElseThrow(() -> new HubDeliveryException(
                        HubDeliveryErrorCode.HUB_DELIVERY_NOT_FOUND,
                        "허브 배송 정보를 찾을 수 없습니다: " + hubDeliveryId
                ));

        return HubDeliveryDetailResponse.from(hubDelivery);
    }

    /**
     * 주문 ID로 조회
     */
    public HubDeliveryDetailResponse findByOrderId(String orderId) {
        HubDelivery hubDelivery = hubDeliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new HubDeliveryException(
                        HubDeliveryErrorCode.HUB_DELIVERY_NOT_FOUND,
                        "해당 주문의 허브 배송 정보를 찾을 수 없습니다: " + orderId
                ));

        return HubDeliveryDetailResponse.from(hubDelivery);
    }

    /**
     * 전체 목록 조회
     */
    public Page<HubDeliveryResponse> findAll(Pageable pageable) {
        return hubDeliveryRepository.findAll(pageable)
                .map(HubDeliveryResponse::from);
    }

    /**
     * 상태별 목록 조회
     */
    public Page<HubDeliveryResponse> findByStatus(HubDeliveryStatus status, Pageable pageable) {
        return hubDeliveryRepository.findByStatus(status, pageable)
                .map(HubDeliveryResponse::from);
    }
}
