package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.persistence.jpa;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.persistence.entity.HubDeliveryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * HubDelivery JPA Repository
 */
public interface HubDeliveryJpaRepository extends JpaRepository<HubDeliveryEntity, String> {

    Optional<HubDeliveryEntity> findByIdAndIsDeletedFalse(String id);

    Optional<HubDeliveryEntity> findByOrderIdAndIsDeletedFalse(String orderId);

    boolean existsByOrderIdAndIsDeletedFalse(String orderId);

    Page<HubDeliveryEntity> findByStatusAndIsDeletedFalse(HubDeliveryStatus status, Pageable pageable);

    Page<HubDeliveryEntity> findByIsDeletedFalse(Pageable pageable);
}