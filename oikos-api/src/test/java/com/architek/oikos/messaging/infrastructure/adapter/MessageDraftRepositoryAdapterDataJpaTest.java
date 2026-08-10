package com.architek.oikos.messaging.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.messaging.infrastructure.mapper.MessageDraftPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MessageDraftRepositoryAdapter.class, MessageDraftPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class MessageDraftRepositoryAdapterDataJpaTest {

    @Autowired
    private MessageDraftRepositoryAdapter adapter;

    private final EntityId propertyId = EntityId.newId();
    private final EntityId owner = EntityId.newId();

    @Test
    void saves_and_finds_a_draft_by_id_with_its_recipients() {
        EntityId recipient = EntityId.newId();
        MessageDraft draft = MessageDraft.create(MessageDraftId.newId(), propertyId, owner, Set.of(recipient), false,
                "Sujet", "Corps");

        adapter.save(draft);

        MessageDraft reloaded = adapter.findById(draft.getId()).orElseThrow();
        assertThat(reloaded.getSubject()).isEqualTo("Sujet");
        assertThat(reloaded.getRecipientUserIds()).containsExactly(recipient);
    }

    @Test
    void saves_a_draft_with_no_recipients_no_subject_and_no_body() {
        MessageDraft draft = MessageDraft.create(MessageDraftId.newId(), propertyId, owner, Set.of(), false, null, null);

        adapter.save(draft);

        MessageDraft reloaded = adapter.findById(draft.getId()).orElseThrow();
        assertThat(reloaded.getSubject()).isNull();
        assertThat(reloaded.getBody()).isNull();
        assertThat(reloaded.getRecipientUserIds()).isEmpty();
    }

    @Test
    void updating_a_draft_persists_changes_without_creating_a_duplicate() {
        MessageDraft draft = MessageDraft.create(MessageDraftId.newId(), propertyId, owner, Set.of(), false, "Old", "Old");
        adapter.save(draft);

        EntityId recipient = EntityId.newId();
        MessageDraft reloaded = adapter.findById(draft.getId()).orElseThrow();
        adapter.save(reloaded.update(Set.of(recipient), false, "New", "New body", Instant.now()));

        MessageDraft updated = adapter.findById(draft.getId()).orElseThrow();
        assertThat(updated.getSubject()).isEqualTo("New");
        assertThat(updated.getRecipientUserIds()).containsExactly(recipient);
    }

    @Test
    void findByCreatedBy_only_returns_the_given_user_s_drafts_paginated() {
        EntityId otherOwner = EntityId.newId();
        adapter.save(MessageDraft.create(MessageDraftId.newId(), propertyId, owner, Set.of(), false, "Mine 1", null));
        adapter.save(MessageDraft.create(MessageDraftId.newId(), propertyId, owner, Set.of(), false, "Mine 2", null));
        adapter.save(MessageDraft.create(MessageDraftId.newId(), propertyId, otherOwner, Set.of(), false, "Not mine", null));

        var page = adapter.findByCreatedBy(owner, PageRequest.of(0, 20), null);

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content()).extracting(MessageDraft::getSubject).containsExactlyInAnyOrder("Mine 1", "Mine 2");
    }

    @Test
    void findByCreatedBy_filters_by_subject_or_body_search_text() {
        adapter.save(MessageDraft.create(MessageDraftId.newId(), propertyId, owner, Set.of(), false, "Fuite d'eau", null));
        adapter.save(MessageDraft.create(MessageDraftId.newId(), propertyId, owner, Set.of(), false, "Autre sujet",
                "mentionne une fuite"));
        adapter.save(MessageDraft.create(MessageDraftId.newId(), propertyId, owner, Set.of(), false, "Sans rapport", null));

        var page = adapter.findByCreatedBy(owner, PageRequest.of(0, 20), "fuite");

        assertThat(page.content()).hasSize(2);
    }

    @Test
    void deleteById_removes_the_draft_and_its_recipients() {
        MessageDraft draft = MessageDraft.create(MessageDraftId.newId(), propertyId, owner, Set.of(EntityId.newId()), false,
                "Sujet", "Corps");
        adapter.save(draft);

        adapter.deleteById(draft.getId());

        assertThat(adapter.findById(draft.getId())).isEmpty();
    }
}
