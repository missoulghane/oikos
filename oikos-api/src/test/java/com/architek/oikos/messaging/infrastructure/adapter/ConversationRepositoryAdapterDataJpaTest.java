package com.architek.oikos.messaging.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.infrastructure.mapper.ConversationPersistenceMapperImpl;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ConversationRepositoryAdapter.class, ConversationPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class ConversationRepositoryAdapterDataJpaTest {

    private static final ConversationSubject SUBJECT = ConversationSubject.of("Sujet");

    @Autowired
    private ConversationRepositoryAdapter adapter;

    @Test
    void saves_and_reloads_a_group_conversation_with_its_participants_and_subject() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipient = EntityId.newId();
        Conversation saved = adapter.save(
                Conversation.createGroup(ConversationId.newId(), propertyId, sender, Set.of(sender, recipient), SUBJECT));

        Conversation reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getParticipantUserIds()).containsExactlyInAnyOrder(sender, recipient);
        assertThat(reloaded.getSubject()).isEqualTo(SUBJECT);
    }

    @Test
    void saves_and_reloads_a_group_conversation_with_more_than_two_participants() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipientA = EntityId.newId();
        EntityId recipientB = EntityId.newId();
        Conversation saved = adapter.save(Conversation.createGroup(ConversationId.newId(), propertyId, sender,
                Set.of(sender, recipientA, recipientB), SUBJECT));

        Conversation reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getParticipantUserIds()).containsExactlyInAnyOrder(sender, recipientA, recipientB);
    }

    @Test
    void reloading_a_broadcast_conversation_has_no_subject() {
        Conversation saved = adapter.save(Conversation.createBroadcast(ConversationId.newId(), EntityId.newId(), EntityId.newId()));

        Conversation reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getSubject()).isNull();
    }

    @Test
    void finds_the_single_broadcast_conversation_of_a_property() {
        EntityId propertyId = EntityId.newId();
        Conversation saved = adapter.save(Conversation.createBroadcast(ConversationId.newId(), propertyId, EntityId.newId()));

        assertThat(adapter.findBroadcastConversation(propertyId)).contains(saved);
        assertThat(adapter.findBroadcastConversation(EntityId.newId())).isEmpty();
    }

    @Test
    void finds_all_group_conversations_a_user_participates_in() {
        EntityId userId = EntityId.newId();
        Conversation first = adapter.save(Conversation.createGroup(ConversationId.newId(), EntityId.newId(), userId,
                Set.of(userId, EntityId.newId()), SUBJECT));
        Conversation second = adapter.save(Conversation.createGroup(ConversationId.newId(), EntityId.newId(), EntityId.newId(),
                Set.of(EntityId.newId(), userId), SUBJECT));
        adapter.save(Conversation.createGroup(ConversationId.newId(), EntityId.newId(), EntityId.newId(),
                Set.of(EntityId.newId(), EntityId.newId()), SUBJECT));

        List<Conversation> found = adapter.findAllGroupByParticipant(userId);

        assertThat(found).extracting(Conversation::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void composing_to_the_same_participants_twice_creates_two_distinct_rows() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipient = EntityId.newId();

        Conversation first =
                adapter.save(Conversation.createGroup(ConversationId.newId(), propertyId, sender, Set.of(sender, recipient), SUBJECT));
        Conversation second =
                adapter.save(Conversation.createGroup(ConversationId.newId(), propertyId, sender, Set.of(sender, recipient), SUBJECT));

        assertThat(first.getId()).isNotEqualTo(second.getId());
        assertThat(adapter.findAllGroupByParticipant(sender)).extracting(Conversation::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void finds_broadcast_conversations_for_a_batch_of_properties() {
        EntityId propertyA = EntityId.newId();
        EntityId propertyB = EntityId.newId();
        EntityId propertyC = EntityId.newId();
        Conversation broadcastA = adapter.save(Conversation.createBroadcast(ConversationId.newId(), propertyA, EntityId.newId()));
        Conversation broadcastB = adapter.save(Conversation.createBroadcast(ConversationId.newId(), propertyB, EntityId.newId()));
        adapter.save(Conversation.createBroadcast(ConversationId.newId(), propertyC, EntityId.newId()));

        List<Conversation> found = adapter.findAllBroadcastByPropertyIds(List.of(propertyA, propertyB));

        assertThat(found).extracting(Conversation::getId).containsExactlyInAnyOrder(broadcastA.getId(), broadcastB.getId());
    }

    @Test
    void finding_broadcast_conversations_for_an_empty_batch_returns_an_empty_list() {
        assertThat(adapter.findAllBroadcastByPropertyIds(List.of())).isEmpty();
    }
}
