package com.architek.oikos.messaging.domain.repository;

import java.util.Optional;

import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface MessageDraftRepository {

    MessageDraft save(MessageDraft draft);

    Optional<MessageDraft> findById(MessageDraftId id);

    /** search matches subject/body substring, case-insensitive; null/blank means no filter. Real
     * DB-level pagination (unlike ConversationAggregator's in-memory approach), since a draft is a
     * single flat table with no cross-source aggregation. */
    Page<MessageDraft> findByCreatedBy(EntityId createdBy, PageRequest pageRequest, String search);

    void deleteById(MessageDraftId id);
}
