package com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.persistence.repository;

import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryErrorCode;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception.HubDeliveryException;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.HubDelivery;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryId;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.model.vo.HubDeliveryStatus;
import com.early_express.hub_delivery_service.domain.hub_delivery.domain.repository.HubDeliveryRepository;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.persistence.entity.HubDeliveryEntity;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.persistence.entity.QHubDeliveryEntity;
import com.early_express.hub_delivery_service.domain.hub_delivery.infrastructure.persistence.jpa.HubDeliveryJpaRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * HubDelivery Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class HubDeliveryRepositoryImpl implements HubDeliveryRepository {

    private final HubDeliveryJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    private static final QHubDeliveryEntity hubDelivery = QHubDeliveryEntity.hubDeliveryEntity;

    @Override
    @Transactional
    public HubDelivery save(HubDelivery hubDelivery) {
        HubDeliveryEntity entity;

        if (hubDelivery.getId() != null) {
            entity = jpaRepository.findByIdAndIsDeletedFalse(hubDelivery.getIdValue())
                    .orElseThrow(() -> new HubDeliveryException(
                            HubDeliveryErrorCode.HUB_DELIVERY_NOT_FOUND,
                            "허브 배송 정보를 찾을 수 없습니다: " + hubDelivery.getIdValue()
                    ));
            entity.updateFromDomain(hubDelivery);
        } else {
            entity = HubDeliveryEntity.fromDomain(hubDelivery);
            entity = jpaRepository.save(entity);
        }

        return entity.toDomain();
    }

    @Override
    public Optional<HubDelivery> findById(HubDeliveryId id) {
        return jpaRepository.findByIdAndIsDeletedFalse(id.getValue())
                .map(HubDeliveryEntity::toDomain);
    }

    @Override
    public Optional<HubDelivery> findByOrderId(String orderId) {
        return jpaRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .map(HubDeliveryEntity::toDomain);
    }

    @Override
    public Page<HubDelivery> findAll(Pageable pageable) {
        List<HubDeliveryEntity> content = queryFactory
                .selectFrom(hubDelivery)
                .where(isNotDeleted())
                .orderBy(hubDelivery.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(hubDelivery.count())
                .from(hubDelivery)
                .where(isNotDeleted());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne)
                .map(HubDeliveryEntity::toDomain);
    }

    @Override
    public Page<HubDelivery> findByStatus(HubDeliveryStatus status, Pageable pageable) {
        List<HubDeliveryEntity> content = queryFactory
                .selectFrom(hubDelivery)
                .where(
                        statusEq(status),
                        isNotDeleted()
                )
                .orderBy(hubDelivery.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(hubDelivery.count())
                .from(hubDelivery)
                .where(
                        statusEq(status),
                        isNotDeleted()
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne)
                .map(HubDeliveryEntity::toDomain);
    }

    @Override
    public boolean existsByOrderId(String orderId) {
        return jpaRepository.existsByOrderIdAndIsDeletedFalse(orderId);
    }

    // ===== BooleanExpression =====

    private BooleanExpression statusEq(HubDeliveryStatus status) {
        return status != null ? hubDelivery.status.eq(status) : null;
    }

    private BooleanExpression isNotDeleted() {
        return hubDelivery.isDeleted.eq(false);
    }
}

