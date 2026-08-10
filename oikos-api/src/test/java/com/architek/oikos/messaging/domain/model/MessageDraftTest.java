package com.architek.oikos.messaging.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class MessageDraftTest {

    @Test
    void a_draft_may_have_no_recipients_no_subject_and_no_body_while_being_composed() {
        MessageDraft draft = MessageDraft.create(MessageDraftId.newId(), EntityId.newId(), EntityId.newId(), Set.of(),
                false, null, null);

        assertThat(draft.getRecipientUserIds()).isEmpty();
        assertThat(draft.getSubject()).isNull();
        assertThat(draft.getBody()).isNull();
        assertThat(draft.isBroadcast()).isFalse();
    }

    @Test
    void a_broadcast_draft_with_stored_recipients_is_rejected() {
        EntityId recipient = EntityId.newId();

        assertThatThrownBy(() -> MessageDraft.create(MessageDraftId.newId(), EntityId.newId(), EntityId.newId(),
                Set.of(recipient), true, null, "Annonce"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updating_a_draft_keeps_its_identity_but_refreshes_its_content_and_last_modified_date() {
        MessageDraftId id = MessageDraftId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId owner = EntityId.newId();
        MessageDraft draft = MessageDraft.reconstruct(id, propertyId, owner, Set.of(), false, "Old subject", "Old body",
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T10:00:00Z"));

        EntityId recipient = EntityId.newId();
        Instant now = Instant.parse("2026-01-02T10:00:00Z");
        MessageDraft updated = draft.update(Set.of(recipient), false, "New subject", "New body", now);

        assertThat(updated.getId()).isEqualTo(id);
        assertThat(updated.getPropertyId()).isEqualTo(propertyId);
        assertThat(updated.getCreatedBy()).isEqualTo(owner);
        assertThat(updated.getRecipientUserIds()).containsExactly(recipient);
        assertThat(updated.getSubject()).isEqualTo("New subject");
        assertThat(updated.getBody()).isEqualTo("New body");
        assertThat(updated.getLastModifiedDate()).isEqualTo(now);
    }

    @Test
    void two_drafts_with_the_same_id_are_equal() {
        MessageDraftId id = MessageDraftId.newId();
        MessageDraft first = MessageDraft.create(id, EntityId.newId(), EntityId.newId(), Set.of(), false, null, null);
        MessageDraft second = MessageDraft.create(id, EntityId.newId(), EntityId.newId(), Set.of(), false, null, null);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }
}
