package com.architek.oikos.meeting.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConvocationDeliveryJpaRepository extends JpaRepository<ConvocationDeliveryEntity, UUID> {

    List<ConvocationDeliveryEntity> findByConvocationIdOrderByCreatedDateAsc(UUID convocationId);

    /**
     * Every delivery of a whole set of convocations, in one query. The tracking
     * table lists a hundred lots and shows each one's attempts; fetching them
     * per row is the N+1 that turns one screen into a hundred queries.
     */
    List<ConvocationDeliveryEntity> findByConvocationIdInOrderByCreatedDateAsc(Collection<UUID> convocationIds);
}
