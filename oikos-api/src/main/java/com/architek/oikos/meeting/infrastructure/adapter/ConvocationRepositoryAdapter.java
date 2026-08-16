package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.ConvocationDelivery;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.infrastructure.mapper.ConvocationPersistenceMapper;
import com.architek.oikos.meeting.infrastructure.persistence.ConvocationDeliveryEntity;
import com.architek.oikos.meeting.infrastructure.persistence.ConvocationDeliveryJpaRepository;
import com.architek.oikos.meeting.infrastructure.persistence.ConvocationEntity;
import com.architek.oikos.meeting.infrastructure.persistence.ConvocationJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The convocation and its deliveries are one aggregate over two tables, and
 * this adapter is what keeps that seam invisible to the rest of the module.
 *
 * <p>Reads always load the deliveries of the whole result set in one query
 * rather than per row: the tracking table lists every lot of the copropriété,
 * and a per-row fetch is the N+1 that turns one screen into a hundred queries.
 *
 * <p>Writes only ever insert deliveries. A delivery is an event - recorded once
 * and never edited - so saving a convocation persists the rows that are new to
 * it and leaves the rest alone. Nothing deletes them either: the point of the
 * change was that an earlier attempt survives a later one.
 */
@Component
public class ConvocationRepositoryAdapter implements ConvocationRepository {

    private final ConvocationJpaRepository jpaRepository;
    private final ConvocationDeliveryJpaRepository deliveryJpaRepository;
    private final ConvocationPersistenceMapper mapper;

    public ConvocationRepositoryAdapter(ConvocationJpaRepository jpaRepository,
                                         ConvocationDeliveryJpaRepository deliveryJpaRepository,
                                         ConvocationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.deliveryJpaRepository = deliveryJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Convocation save(Convocation convocation) {
        ConvocationEntity saved = jpaRepository.save(toManagedEntity(convocation));
        return mapper.toDomain(saved, saveNewDeliveries(convocation));
    }

    @Override
    public List<Convocation> saveAll(List<Convocation> convocations) {
        List<ConvocationEntity> entities = convocations.stream().map(this::toManagedEntity).toList();
        List<ConvocationEntity> saved = jpaRepository.saveAll(entities);

        Map<UUID, List<ConvocationDelivery>> deliveriesById = new LinkedHashMap<>();
        for (Convocation convocation : convocations) {
            deliveriesById.put(convocation.getId().asUuid(), saveNewDeliveries(convocation));
        }
        return saved.stream()
                .map(entity -> mapper.toDomain(entity, deliveriesById.getOrDefault(entity.getId(), List.of())))
                .toList();
    }

    @Override
    public Optional<Convocation> findById(ConvocationId id) {
        return jpaRepository.findById(id.asUuid())
                .map(entity -> mapper.toDomain(entity, deliveriesOf(entity.getId())));
    }

    @Override
    public Optional<Convocation> findByConfirmationToken(String confirmationToken) {
        return jpaRepository.findByConfirmationToken(confirmationToken)
                .map(entity -> mapper.toDomain(entity, deliveriesOf(entity.getId())));
    }

    @Override
    public List<Convocation> findByGeneralMeetingId(GeneralMeetingId generalMeetingId) {
        return toDomain(jpaRepository.findByGeneralMeetingId(generalMeetingId.asUuid()));
    }

    @Override
    public List<Convocation> findByGeneralMeetingIdAndUnitIds(GeneralMeetingId generalMeetingId,
                                                                Collection<EntityId> unitIds) {
        if (unitIds.isEmpty()) {
            return List.of();
        }
        return toDomain(jpaRepository.findByGeneralMeetingIdAndUnitIdIn(generalMeetingId.asUuid(), toUuids(unitIds)));
    }

    @Override
    public List<Convocation> findByUnitIds(Collection<EntityId> unitIds) {
        if (unitIds.isEmpty()) {
            return List.of();
        }
        return toDomain(jpaRepository.findByUnitIdIn(toUuids(unitIds)));
    }

    private List<Convocation> toDomain(List<ConvocationEntity> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<ConvocationDelivery>> deliveriesByConvocation = deliveriesOf(
                entities.stream().map(ConvocationEntity::getId).toList());
        return entities.stream()
                .map(entity -> mapper.toDomain(entity,
                        deliveriesByConvocation.getOrDefault(entity.getId(), List.of())))
                .toList();
    }

    private List<ConvocationDelivery> deliveriesOf(UUID convocationId) {
        return deliveryJpaRepository.findByConvocationIdOrderByCreatedDateAsc(convocationId).stream()
                .map(mapper::toDomain).toList();
    }

    private Map<UUID, List<ConvocationDelivery>> deliveriesOf(Collection<UUID> convocationIds) {
        Map<UUID, List<ConvocationDelivery>> byConvocation = new LinkedHashMap<>();
        for (ConvocationDeliveryEntity entity : deliveryJpaRepository
                .findByConvocationIdInOrderByCreatedDateAsc(convocationIds)) {
            byConvocation.computeIfAbsent(entity.getConvocationId(), key -> new ArrayList<>())
                    .add(mapper.toDomain(entity));
        }
        return byConvocation;
    }

    /**
     * Inserts the deliveries this convocation carries that are not in the table
     * yet, and returns the full list as it now stands. Existing ones are left
     * untouched rather than re-saved: they are immutable events, and rewriting
     * them would only risk bumping their audit columns for nothing.
     */
    private List<ConvocationDelivery> saveNewDeliveries(Convocation convocation) {
        List<ConvocationDelivery> deliveries = convocation.getDeliveries();
        if (deliveries.isEmpty()) {
            return List.of();
        }
        for (ConvocationDelivery delivery : deliveries) {
            if (!deliveryJpaRepository.existsById(delivery.getId().asUuid())) {
                deliveryJpaRepository.save(mapper.toEntity(delivery, convocation.getId()));
            }
        }
        return deliveriesOf(convocation.getId().asUuid());
    }

    private static List<UUID> toUuids(Collection<EntityId> ids) {
        return ids.stream().map(EntityId::value).toList();
    }

    private ConvocationEntity toManagedEntity(Convocation convocation) {
        ConvocationEntity entity = jpaRepository.findById(convocation.getId().asUuid())
                .orElseGet(ConvocationEntity::new);
        return mapper.toEntity(convocation, entity);
    }
}
