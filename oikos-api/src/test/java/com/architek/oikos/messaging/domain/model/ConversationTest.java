package com.architek.oikos.messaging.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class ConversationTest {

    private static final ConversationSubject SUBJECT = ConversationSubject.of("Fuite d'eau");

    @Test
    void creating_a_group_conversation_with_two_participants_stores_both_and_its_subject() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipient = EntityId.newId();

        Conversation conversation = Conversation.createGroup(ConversationId.newId(), propertyId, sender,
                Set.of(sender, recipient), SUBJECT, null);

        assertThat(conversation.getType()).isEqualTo(ConversationType.GROUP);
        assertThat(conversation.getParticipantUserIds()).containsExactlyInAnyOrder(sender, recipient);
        assertThat(conversation.getSubject()).isEqualTo(SUBJECT);
        assertThat(conversation.hasParticipant(sender)).isTrue();
        assertThat(conversation.hasParticipant(recipient)).isTrue();
        assertThat(conversation.hasParticipant(EntityId.newId())).isFalse();
    }

    @Test
    void creating_a_group_conversation_with_more_than_two_participants_stores_them_all() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipientA = EntityId.newId();
        EntityId recipientB = EntityId.newId();

        Conversation conversation = Conversation.createGroup(ConversationId.newId(), propertyId, sender,
                Set.of(sender, recipientA, recipientB), SUBJECT, null);

        assertThat(conversation.getType()).isEqualTo(ConversationType.GROUP);
        assertThat(conversation.getParticipantUserIds()).containsExactlyInAnyOrder(sender, recipientA, recipientB);
    }

    @Test
    void creating_a_broadcast_conversation_stores_no_participant_and_no_subject() {
        Conversation conversation = Conversation.createBroadcast(ConversationId.newId(), EntityId.newId(), EntityId.newId());

        assertThat(conversation.getType()).isEqualTo(ConversationType.BROADCAST);
        assertThat(conversation.getParticipantUserIds()).isEmpty();
        assertThat(conversation.getSubject()).isNull();
    }

    @Test
    void reconstructing_a_group_conversation_with_fewer_than_two_participants_is_rejected() {
        assertThatThrownBy(() -> Conversation.reconstruct(ConversationId.newId(), EntityId.newId(), ConversationType.GROUP,
                EntityId.newId(), Set.of(EntityId.newId()), SUBJECT, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reconstructing_a_broadcast_conversation_with_a_participant_is_rejected() {
        assertThatThrownBy(() -> Conversation.reconstruct(ConversationId.newId(), EntityId.newId(), ConversationType.BROADCAST,
                EntityId.newId(), Set.of(EntityId.newId()), null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reconstructing_a_group_conversation_without_a_subject_is_rejected() {
        EntityId sender = EntityId.newId();
        EntityId recipient = EntityId.newId();

        assertThatThrownBy(() -> Conversation.reconstruct(ConversationId.newId(), EntityId.newId(), ConversationType.GROUP,
                sender, Set.of(sender, recipient), null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reconstructing_a_broadcast_conversation_with_a_subject_is_rejected() {
        assertThatThrownBy(() -> Conversation.reconstruct(ConversationId.newId(), EntityId.newId(), ConversationType.BROADCAST,
                EntityId.newId(), Set.of(), SUBJECT, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void creating_a_group_conversation_with_a_non_group_type_and_a_concerns_unit_is_rejected() {
        assertThatThrownBy(() -> Conversation.reconstruct(ConversationId.newId(), EntityId.newId(), ConversationType.BROADCAST,
                EntityId.newId(), Set.of(), null, "Appartement 3", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void two_conversations_with_the_same_id_are_equal() {
        ConversationId id = ConversationId.newId();
        Conversation first = Conversation.createBroadcast(id, EntityId.newId(), EntityId.newId());
        Conversation second = Conversation.createBroadcast(id, EntityId.newId(), EntityId.newId());

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }
}
