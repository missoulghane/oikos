package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.dto.ConversationParticipantView;
import com.architek.oikos.messaging.application.dto.ConversationSummaryView;
import com.architek.oikos.messaging.application.port.out.PropertyMemberDirectoryPort;
import com.architek.oikos.messaging.application.query.ConversationBox;
import com.architek.oikos.messaging.application.query.ConversationReadState;
import com.architek.oikos.messaging.application.port.out.UserAccessPort;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.ConversationReadMarker;
import com.architek.oikos.messaging.domain.model.ConversationType;
import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.repository.ConversationReadMarkerRepository;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ConversationAggregatorTest {

    private static final ConversationSubject SUBJECT = ConversationSubject.of("Sujet");

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationReadMarkerRepository readMarkerRepository;

    @Mock
    private UserAccessPort userAccessPort;

    @Mock
    private PropertyMemberDirectoryPort propertyMemberDirectoryPort;

    @Mock
    private MemberDisplayNameResolver memberDisplayNameResolver;

    private ConversationAggregator newAggregator() {
        return new ConversationAggregator(conversationRepository, messageRepository, readMarkerRepository, userAccessPort,
                propertyMemberDirectoryPort, memberDisplayNameResolver);
    }

    @Test
    void aggregates_group_and_broadcast_conversations_sorted_by_last_message_desc_with_no_message_last() {
        EntityId caller = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId other = EntityId.newId();

        ConversationId groupId = ConversationId.newId();
        Conversation group = Conversation.createGroup(groupId, propertyId, caller, Set.of(caller, other), SUBJECT, null);
        ConversationId broadcastId = ConversationId.newId();
        Conversation broadcast = Conversation.createBroadcast(broadcastId, propertyId, other, ConversationSubject.of("Annonce"));
        ConversationId emptyGroupId = ConversationId.newId();
        EntityId other2 = EntityId.newId();
        Conversation emptyGroup = Conversation.createGroup(emptyGroupId, propertyId, caller, Set.of(caller, other2), SUBJECT, null);

        when(conversationRepository.findAllGroupByParticipant(caller)).thenReturn(List.of(group, emptyGroup));
        when(userAccessPort.memberPropertyIds(caller)).thenReturn(Set.of(propertyId));
        when(conversationRepository.findAllByPropertyIdsAndType(Set.of(propertyId), ConversationType.BROADCAST)).thenReturn(List.of(broadcast));

        when(propertyMemberDirectoryPort.getPropertyName(propertyId)).thenReturn("Copro Test");
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(other, "Other Name", other2, "Other2 Name"));

        Message groupLastMessage = Message.post(MessageId.newId(), groupId, other, SenderIdentity.OWNER, MessageBody.of("Hi"),
                Instant.parse("2026-01-01T10:00:00Z"));
        when(messageRepository.findLastMessage(groupId)).thenReturn(Optional.of(groupLastMessage));
        Message broadcastLastMessage = Message.post(MessageId.newId(), broadcastId, other, SenderIdentity.BOARD,
                MessageBody.of("Annonce"), Instant.parse("2026-01-02T10:00:00Z"));
        when(messageRepository.findLastMessage(broadcastId)).thenReturn(Optional.of(broadcastLastMessage));
        when(messageRepository.findLastMessage(emptyGroupId)).thenReturn(Optional.empty());

        when(readMarkerRepository.findByConversationIdAndUserId(groupId, caller)).thenReturn(Optional.empty());
        when(readMarkerRepository.findByConversationIdAndUserId(broadcastId, caller)).thenReturn(Optional.empty());
        when(readMarkerRepository.findByConversationIdAndUserId(emptyGroupId, caller)).thenReturn(Optional.empty());
        when(messageRepository.countUnread(groupId, null)).thenReturn(1L);
        when(messageRepository.countUnread(broadcastId, null)).thenReturn(3L);
        when(messageRepository.countUnread(emptyGroupId, null)).thenReturn(0L);

        List<ConversationSummaryView> views = newAggregator().listAll(caller, null);

        assertThat(views).hasSize(3);
        assertThat(views.get(0).id()).isEqualTo(broadcastId);
        assertThat(views.get(1).id()).isEqualTo(groupId);
        assertThat(views.get(2).id()).isEqualTo(emptyGroupId);
        assertThat(views.get(2).lastMessageAt()).isNull();

        ConversationSummaryView groupView = views.get(1);
        assertThat(groupView.type()).isEqualTo(ConversationType.GROUP);
        assertThat(groupView.subject()).isEqualTo(SUBJECT.value());
        assertThat(groupView.participants()).containsExactly(new ConversationParticipantView(other, "Other Name"));
        assertThat(groupView.unreadCount()).isEqualTo(1L);

        ConversationSummaryView broadcastView = views.get(0);
        assertThat(broadcastView.type()).isEqualTo(ConversationType.BROADCAST);
        assertThat(broadcastView.subject()).isEqualTo("Annonce");
        assertThat(broadcastView.participants()).isEmpty();
        assertThat(broadcastView.unreadCount()).isEqualTo(3L);
    }

    @Test
    void aggregates_a_three_participant_group_with_every_other_participant_resolved() {
        EntityId caller = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId recipientA = EntityId.newId();
        EntityId recipientB = EntityId.newId();
        ConversationId groupId = ConversationId.newId();
        Conversation group = Conversation.createGroup(groupId, propertyId, caller, Set.of(caller, recipientA, recipientB), SUBJECT, null);

        when(conversationRepository.findAllGroupByParticipant(caller)).thenReturn(List.of(group));
        when(userAccessPort.memberPropertyIds(caller)).thenReturn(Set.of());
        when(conversationRepository.findAllByPropertyIdsAndType(Set.of(), ConversationType.BROADCAST)).thenReturn(List.of());
        when(propertyMemberDirectoryPort.getPropertyName(propertyId)).thenReturn("Résidence Alpha");
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(recipientA, "Alice", recipientB, "Bob"));
        when(messageRepository.findLastMessage(groupId)).thenReturn(Optional.empty());
        when(readMarkerRepository.findByConversationIdAndUserId(groupId, caller)).thenReturn(Optional.empty());
        when(messageRepository.countUnread(groupId, null)).thenReturn(0L);

        List<ConversationSummaryView> views = newAggregator().listAll(caller, null);

        assertThat(views.get(0).participants()).containsExactlyInAnyOrder(
                new ConversationParticipantView(recipientA, "Alice"), new ConversationParticipantView(recipientB, "Bob"));
    }

    @Test
    void filters_by_search_on_property_name_or_any_participant_name() {
        EntityId caller = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId other = EntityId.newId();
        ConversationId groupId = ConversationId.newId();
        Conversation group = Conversation.createGroup(groupId, propertyId, caller, Set.of(caller, other), SUBJECT, null);

        when(conversationRepository.findAllGroupByParticipant(caller)).thenReturn(List.of(group));
        when(userAccessPort.memberPropertyIds(caller)).thenReturn(Set.of());
        when(conversationRepository.findAllByPropertyIdsAndType(Set.of(), ConversationType.BROADCAST)).thenReturn(List.of());
        when(propertyMemberDirectoryPort.getPropertyName(propertyId)).thenReturn("Résidence Alpha");
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(other, "Jean Dupont"));
        when(messageRepository.findLastMessage(groupId)).thenReturn(Optional.empty());
        when(readMarkerRepository.findByConversationIdAndUserId(groupId, caller)).thenReturn(Optional.empty());
        when(messageRepository.countUnread(groupId, null)).thenReturn(0L);

        assertThat(newAggregator().listAll(caller, "dupont")).hasSize(1);
        assertThat(newAggregator().listAll(caller, "nomatch")).isEmpty();
    }

    // Chercher « A12 » doit ramener le fil ouvert avec le propriétaire de ce lot,
    // pas seulement celui dont l'objet cite le numéro.
    @Test
    void filters_by_search_on_a_participant_s_unit_number() {
        EntityId caller = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId other = EntityId.newId();
        ConversationId groupId = ConversationId.newId();
        Conversation group = Conversation.createGroup(groupId, propertyId, caller, Set.of(caller, other), SUBJECT, null);

        when(conversationRepository.findAllGroupByParticipant(caller)).thenReturn(List.of(group));
        when(userAccessPort.memberPropertyIds(caller)).thenReturn(Set.of());
        when(conversationRepository.findAllByPropertyIdsAndType(Set.of(), ConversationType.BROADCAST)).thenReturn(List.of());
        when(propertyMemberDirectoryPort.getPropertyName(propertyId)).thenReturn("Résidence Alpha");
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(other, "Jean Dupont"));
        when(memberDisplayNameResolver.unitNumbersByUserId(propertyId)).thenReturn(Map.of(other, List.of("A12")));
        when(messageRepository.findLastMessage(groupId)).thenReturn(Optional.empty());
        when(readMarkerRepository.findByConversationIdAndUserId(groupId, caller)).thenReturn(Optional.empty());
        when(messageRepository.countUnread(groupId, null)).thenReturn(0L);

        assertThat(newAggregator().listAll(caller, "a12")).hasSize(1);
        assertThat(newAggregator().listAll(caller, "b7")).isEmpty();
    }

    @Test
    void read_state_filters_to_the_conversations_holding_unread_messages_or_none() {
        EntityId caller = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId other = EntityId.newId();

        ConversationId unreadId = ConversationId.newId();
        Conversation unread = Conversation.createGroup(unreadId, propertyId, caller, Set.of(caller, other), SUBJECT, null);
        ConversationId readId = ConversationId.newId();
        Conversation read = Conversation.createGroup(readId, propertyId, caller, Set.of(caller, other), SUBJECT, null);

        when(conversationRepository.findAllGroupByParticipant(caller)).thenReturn(List.of(unread, read));
        when(userAccessPort.memberPropertyIds(caller)).thenReturn(Set.of());
        when(conversationRepository.findAllByPropertyIdsAndType(Set.of(), ConversationType.BROADCAST)).thenReturn(List.of());
        when(propertyMemberDirectoryPort.getPropertyName(propertyId)).thenReturn("Résidence Alpha");
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(other, "Jean Dupont"));
        when(messageRepository.findLastMessage(unreadId)).thenReturn(Optional.empty());
        when(messageRepository.findLastMessage(readId)).thenReturn(Optional.empty());
        when(readMarkerRepository.findByConversationIdAndUserId(unreadId, caller)).thenReturn(Optional.empty());
        when(readMarkerRepository.findByConversationIdAndUserId(readId, caller)).thenReturn(Optional.empty());
        when(messageRepository.countUnread(unreadId, null)).thenReturn(2L);
        when(messageRepository.countUnread(readId, null)).thenReturn(0L);

        assertThat(newAggregator().listAll(caller, null, null, ConversationReadState.UNREAD))
                .extracting(ConversationSummaryView::id).containsExactly(unreadId);
        assertThat(newAggregator().listAll(caller, null, null, ConversationReadState.READ))
                .extracting(ConversationSummaryView::id).containsExactly(readId);
    }

    @Test
    void box_filters_to_conversations_the_caller_sent_into_or_received_into() {
        EntityId caller = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId other = EntityId.newId();

        // sentOnly: only the caller ever posted - Envoyé only, never Réception.
        ConversationId sentOnlyId = ConversationId.newId();
        Conversation sentOnly = Conversation.createGroup(sentOnlyId, propertyId, caller, Set.of(caller, other), SUBJECT, null);
        // receivedOnly: only the other participant ever posted - Réception only, never Envoyé.
        ConversationId receivedOnlyId = ConversationId.newId();
        Conversation receivedOnly = Conversation.createGroup(receivedOnlyId, propertyId, other, Set.of(caller, other), SUBJECT, null);
        // both: a back-and-forth - visible in both boxes.
        ConversationId bothId = ConversationId.newId();
        Conversation both = Conversation.createGroup(bothId, propertyId, caller, Set.of(caller, other), SUBJECT, null);

        when(conversationRepository.findAllGroupByParticipant(caller)).thenReturn(List.of(sentOnly, receivedOnly, both));
        when(userAccessPort.memberPropertyIds(caller)).thenReturn(Set.of());
        when(conversationRepository.findAllByPropertyIdsAndType(Set.of(), ConversationType.BROADCAST)).thenReturn(List.of());
        when(propertyMemberDirectoryPort.getPropertyName(propertyId)).thenReturn("Résidence Alpha");
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(other, "Jean Dupont"));

        when(messageRepository.findLastMessage(sentOnlyId)).thenReturn(Optional.empty());
        when(messageRepository.findLastMessage(receivedOnlyId)).thenReturn(Optional.empty());
        when(messageRepository.findLastMessage(bothId)).thenReturn(Optional.empty());
        when(messageRepository.countByConversation(sentOnlyId)).thenReturn(1L);
        when(messageRepository.countByConversation(receivedOnlyId)).thenReturn(1L);
        when(messageRepository.countByConversation(bothId)).thenReturn(2L);
        when(messageRepository.countByConversationAndSender(sentOnlyId, caller)).thenReturn(1L);
        when(messageRepository.countByConversationAndSender(receivedOnlyId, caller)).thenReturn(0L);
        when(messageRepository.countByConversationAndSender(bothId, caller)).thenReturn(1L);

        when(readMarkerRepository.findByConversationIdAndUserId(sentOnlyId, caller)).thenReturn(Optional.empty());
        when(readMarkerRepository.findByConversationIdAndUserId(receivedOnlyId, caller)).thenReturn(Optional.empty());
        when(readMarkerRepository.findByConversationIdAndUserId(bothId, caller)).thenReturn(Optional.empty());
        when(messageRepository.countUnread(sentOnlyId, null)).thenReturn(0L);
        when(messageRepository.countUnread(receivedOnlyId, null)).thenReturn(0L);
        when(messageRepository.countUnread(bothId, null)).thenReturn(0L);

        List<ConversationSummaryView> sent = newAggregator().listAll(caller, null, ConversationBox.SENT);
        assertThat(sent).extracting(ConversationSummaryView::id).containsExactlyInAnyOrder(sentOnlyId, bothId);

        List<ConversationSummaryView> received = newAggregator().listAll(caller, null, ConversationBox.RECEIVED);
        assertThat(received).extracting(ConversationSummaryView::id).containsExactlyInAnyOrder(receivedOnlyId, bothId);
    }

    @Test
    void an_unread_marker_pointing_at_a_message_narrows_the_unread_count() {
        EntityId caller = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId other = EntityId.newId();
        ConversationId groupId = ConversationId.newId();
        Conversation group = Conversation.createGroup(groupId, propertyId, caller, Set.of(caller, other), SUBJECT, null);
        MessageId lastReadId = MessageId.newId();
        ConversationReadMarker marker = ConversationReadMarker.reconstruct(groupId, caller, lastReadId, Instant.EPOCH);

        when(conversationRepository.findAllGroupByParticipant(caller)).thenReturn(List.of(group));
        when(userAccessPort.memberPropertyIds(caller)).thenReturn(Set.of());
        when(conversationRepository.findAllByPropertyIdsAndType(Set.of(), ConversationType.BROADCAST)).thenReturn(List.of());
        when(propertyMemberDirectoryPort.getPropertyName(propertyId)).thenReturn("Résidence Alpha");
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(other, "Jean Dupont"));
        when(messageRepository.findLastMessage(groupId)).thenReturn(Optional.empty());
        when(readMarkerRepository.findByConversationIdAndUserId(groupId, caller)).thenReturn(Optional.of(marker));
        when(messageRepository.countUnread(groupId, lastReadId)).thenReturn(2L);

        List<ConversationSummaryView> views = newAggregator().listAll(caller, null);

        assertThat(views.get(0).unreadCount()).isEqualTo(2L);
    }
}
