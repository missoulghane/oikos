package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.command.SendMessageCommand;
import com.architek.oikos.messaging.application.dto.MessageView;
import com.architek.oikos.messaging.application.port.out.UserAccessPort;
import com.architek.oikos.messaging.domain.exception.ConversationNotFoundException;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.UnauthorizedException;

@ExtendWith(MockitoExtension.class)
class SendMessageServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
    private static final ConversationSubject SUBJECT = ConversationSubject.of("Sujet");

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MemberDisplayNameResolver memberDisplayNameResolver;

    @Mock
    private UserAccessPort userAccessPort;

    @Mock
    private SenderIdentityValidator senderIdentityValidator;

    @BeforeEach
    void resolvesAsOwnerByDefault() {
        lenient().when(senderIdentityValidator.resolve(any(), any(), any())).thenReturn(SenderIdentity.OWNER);
    }

    private SendMessageService newService() {
        return new SendMessageService(conversationRepository, messageRepository, memberDisplayNameResolver, userAccessPort,
                senderIdentityValidator, CLOCK);
    }

    @Test
    void a_participant_can_send_a_message_in_a_group_conversation() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipient = EntityId.newId();
        ConversationId conversationId = ConversationId.newId();
        Conversation conversation = Conversation.createGroup(conversationId, propertyId, sender, Set.of(sender, recipient), SUBJECT, null);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(sender, "Jane Doe"));

        MessageView view = newService().send(new SendMessageCommand(conversationId, sender, MessageBody.of("Bonjour"),
                SenderIdentity.OWNER));

        assertThat(view.body()).isEqualTo("Bonjour");
        assertThat(view.senderName()).isEqualTo("Jane Doe");
        assertThat(view.senderIdentity()).isEqualTo(SenderIdentity.OWNER);
        assertThat(view.mine()).isTrue();
    }

    @Test
    void a_non_participant_cannot_send_a_message_in_a_group_conversation() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipient = EntityId.newId();
        EntityId stranger = EntityId.newId();
        ConversationId conversationId = ConversationId.newId();
        Conversation conversation = Conversation.createGroup(conversationId, propertyId, sender, Set.of(sender, recipient), SUBJECT, null);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> newService().send(new SendMessageCommand(conversationId, stranger, MessageBody.of("Hi"),
                SenderIdentity.OWNER)))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void a_board_or_manager_member_can_send_a_message_in_a_broadcast_conversation() {
        EntityId propertyId = EntityId.newId();
        EntityId creator = EntityId.newId();
        ConversationId conversationId = ConversationId.newId();
        Conversation conversation = Conversation.createBroadcast(conversationId, propertyId, creator);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userAccessPort.canBroadcast(creator, propertyId)).thenReturn(true);
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of());

        MessageView view = newService().send(new SendMessageCommand(conversationId, creator, MessageBody.of("Annonce"), null));

        assertThat(view.body()).isEqualTo("Annonce");
        assertThat(view.senderIdentity()).isEqualTo(SenderIdentity.BOARD);
    }

    @Test
    void a_plain_member_without_broadcast_rights_cannot_send_a_message_in_a_broadcast_conversation() {
        EntityId propertyId = EntityId.newId();
        EntityId creator = EntityId.newId();
        EntityId plainOwner = EntityId.newId();
        ConversationId conversationId = ConversationId.newId();
        Conversation conversation = Conversation.createBroadcast(conversationId, propertyId, creator);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userAccessPort.canBroadcast(plainOwner, propertyId)).thenReturn(false);

        assertThatThrownBy(() -> newService().send(new SendMessageCommand(conversationId, plainOwner, MessageBody.of("Hi"), null)))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void a_staff_member_can_send_a_message_in_a_board_private_conversation_always_as_board() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        ConversationId conversationId = ConversationId.newId();
        Conversation conversation = Conversation.createBoardPrivate(conversationId, propertyId, sender, SUBJECT);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userAccessPort.managesProperty(sender, propertyId)).thenReturn(true);
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(sender, "Jane Doe"));

        // Even a client that (wrongly) claims OWNER must be posted as BOARD.
        MessageView view = newService().send(new SendMessageCommand(conversationId, sender, MessageBody.of("Devis"),
                SenderIdentity.OWNER));

        assertThat(view.senderIdentity()).isEqualTo(SenderIdentity.BOARD);
    }

    @Test
    void a_plain_owner_cannot_send_a_message_in_a_board_private_conversation() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId plainOwner = EntityId.newId();
        ConversationId conversationId = ConversationId.newId();
        Conversation conversation = Conversation.createBoardPrivate(conversationId, propertyId, sender, SUBJECT);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userAccessPort.managesProperty(plainOwner, propertyId)).thenReturn(false);

        assertThatThrownBy(() -> newService().send(new SendMessageCommand(conversationId, plainOwner, MessageBody.of("Hi"), null)))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void sending_a_message_to_an_unknown_conversation_is_rejected() {
        ConversationId conversationId = ConversationId.newId();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().send(new SendMessageCommand(conversationId, EntityId.newId(), MessageBody.of("Hi"), null)))
                .isInstanceOf(ConversationNotFoundException.class);
    }
}
