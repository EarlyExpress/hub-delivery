package com.early_express.hub_delivery_service.domain.hub_delivery.domain.repository;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryId;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Hub Delivery Domain Repository Interface
 */
public interface HubDeliveryRepository {

    HubDelivery save(HubDelivery hubDelivery);

    Optional<HubDelivery> findById(HubDeliveryId id);

    Optional<HubDelivery> findByOrderId(String orderId);

    Page<HubDelivery> findAll(Pageable pageable);

    Page<HubDelivery> findByStatus(HubDeliveryStatus status, Pageable pageable);

    boolean existsByOrderId(String orderId);
}
