package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.command.StartGroupConversationCommand;
import com.architek.oikos.messaging.application.port.out.UserAccessPort;
import com.architek.oikos.messaging.domain.exception.RecipientNotPropertyMemberException;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class StartGroupConversationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
    private static final ConversationSubject SUBJECT = ConversationSubject.of("Sujet");
    private static final MessageBody BODY = MessageBody.of("Bonjour");

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserAccessPort userAccessPort;

    @Mock
    private MemberDisplayNameResolver memberDisplayNameResolver;

    @Mock
    private SenderIdentityValidator senderIdentityValidator;

    @BeforeEach
    void resolvesAsOwnerByDefault() {
        // Not every test reaches identity resolution (some fail validation earlier) - lenient so
        // those don't trip strict-stubbing.
        lenient().when(senderIdentityValidator.resolve(any(), any(), any())).thenReturn(SenderIdentity.OWNER);
    }

    private StartGroupConversationService newService() {
        return new StartGroupConversationService(conversationRepository, messageRepository, userAccessPort,
                memberDisplayNameResolver, senderIdentityValidator, CLOCK);
    }

    @Test
    void starting_a_conversation_with_a_single_recipient_creates_it_with_its_subject_and_posts_the_first_message() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipient = EntityId.newId();
        when(userAccessPort.isMember(sender, propertyId)).thenReturn(true);
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(recipient, "Recipient"));
        when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationId id = newService().start(new StartGroupConversationCommand(propertyId, sender, Set.of(recipient),
                SUBJECT, BODY, SenderIdentity.OWNER, null));

        assertThat(id).isNotNull();
        verify(conversationRepository).save(argThat(conversation -> conversation.getSubject().equals(SUBJECT)));
    }

    @Test
    void starting_a_conversation_with_several_recipients_creates_a_group_with_every_participant() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipientA = EntityId.newId();
        EntityId recipientB = EntityId.newId();
        when(userAccessPort.isMember(sender, propertyId)).thenReturn(true);
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(recipientA, "A", recipientB, "B"));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationId id = newService().start(new StartGroupConversationCommand(propertyId, sender,
                Set.of(recipientA, recipientB), SUBJECT, BODY, SenderIdentity.OWNER, null));

        assertThat(id).isNotNull();
    }

    @Test
    void composing_to_the_same_recipients_twice_creates_two_distinct_conversations() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipient = EntityId.newId();
        when(userAccessPort.isMember(sender, propertyId)).thenReturn(true);
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(recipient, "Recipient"));
        when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationId first = newService().start(new StartGroupConversationCommand(propertyId, sender, Set.of(recipient),
                SUBJECT, BODY, SenderIdentity.OWNER, null));
        ConversationId second = newService().start(new StartGroupConversationCommand(propertyId, sender, Set.of(recipient),
                SUBJECT, BODY, SenderIdentity.OWNER, null));

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void starting_a_conversation_with_an_empty_recipient_list_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();

        assertThatThrownBy(() -> newService().start(new StartGroupConversationCommand(propertyId, sender, Set.of(), SUBJECT,
                BODY, SenderIdentity.OWNER, null)))
                .isInstanceOf(RecipientNotPropertyMemberException.class);
    }

    @Test
    void starting_a_conversation_with_yourself_as_a_recipient_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();

        assertThatThrownBy(() -> newService().start(new StartGroupConversationCommand(propertyId, sender, Set.of(sender),
                SUBJECT, BODY, SenderIdentity.OWNER, null)))
                .isInstanceOf(RecipientNotPropertyMemberException.class);
    }

    @Test
    void starting_a_conversation_as_a_non_member_sender_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipient = EntityId.newId();
        when(userAccessPort.isMember(sender, propertyId)).thenReturn(false);

        assertThatThrownBy(() -> newService().start(new StartGroupConversationCommand(propertyId, sender, Set.of(recipient),
                SUBJECT, BODY, SenderIdentity.OWNER, null)))
                .isInstanceOf(RecipientNotPropertyMemberException.class);
    }

    @Test
    void starting_a_conversation_with_a_recipient_who_is_not_a_property_member_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipient = EntityId.newId();
        when(userAccessPort.isMember(sender, propertyId)).thenReturn(true);
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of());

        assertThatThrownBy(() -> newService().start(new StartGroupConversationCommand(propertyId, sender, Set.of(recipient),
                SUBJECT, BODY, SenderIdentity.OWNER, null)))
                .isInstanceOf(RecipientNotPropertyMemberException.class);
    }

    @Test
    void starting_a_conversation_with_one_recipient_lacking_a_linked_account_among_several_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipientWithAccount = EntityId.newId();
        EntityId recipientWithoutAccount = EntityId.newId();
        when(userAccessPort.isMember(sender, propertyId)).thenReturn(true);
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(recipientWithAccount, "Has Account"));

        assertThatThrownBy(() -> newService().start(new StartGroupConversationCommand(propertyId, sender,
                Set.of(recipientWithAccount, recipientWithoutAccount), SUBJECT, BODY, SenderIdentity.OWNER, null)))
                .isInstanceOf(RecipientNotPropertyMemberException.class);
    }

    @Test
    void starting_a_conversation_with_an_identity_the_sender_does_not_hold_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        EntityId recipient = EntityId.newId();
        when(userAccessPort.isMember(sender, propertyId)).thenReturn(true);
        when(senderIdentityValidator.resolve(sender, propertyId, SenderIdentity.BOARD))
                .thenThrow(new com.architek.oikos.shared.exception.UnauthorizedException("not eligible"));

        assertThatThrownBy(() -> newService().start(new StartGroupConversationCommand(propertyId, sender, Set.of(recipient),
                SUBJECT, BODY, SenderIdentity.BOARD, null)))
                .isInstanceOf(com.architek.oikos.shared.exception.UnauthorizedException.class);
    }
}
