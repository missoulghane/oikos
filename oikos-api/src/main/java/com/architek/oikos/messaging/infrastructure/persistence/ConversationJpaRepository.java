package com.architek.oikos.messaging.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.architek.oikos.messaging.domain.model.ConversationType;

public interface ConversationJpaRepository extends JpaRepository<ConversationEntity, UUID> {

    Optional<ConversationEntity> findByPropertyIdAndType(UUID propertyId, ConversationType type);

    @Query("select c from ConversationEntity c join c.participantUserIds p "
            + "where c.type = com.architek.oikos.messaging.domain.model.ConversationType.GROUP and p = :userId")
    List<ConversationEntity> findAllGroupByParticipant(@Param("userId") UUID userId);

    List<ConversationEntity> findByPropertyIdInAndType(Collection<UUID> propertyIds, ConversationType type);
}
