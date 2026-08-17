package com.architek.oikos.meeting.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConvocationReplyJpaRepository extends JpaRepository<ConvocationReplyEntity, UUID> {

    List<ConvocationReplyEntity> findByConvocationIdOrderByCreatedDateAsc(UUID convocationId);

    /**
     * Every answer of a whole set of convocations, in one query - same reason as
     * the deliveries': the tracking table lists every lot of the copropriété,
     * and a per-row fetch is the N+1 that turns one screen into a hundred
     * queries.
     */
    List<ConvocationReplyEntity> findByConvocationIdInOrderByCreatedDateAsc(Collection<UUID> convocationIds);
}
