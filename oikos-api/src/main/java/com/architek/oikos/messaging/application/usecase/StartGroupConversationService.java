package com.architek.oikos.messaging.application.usecase;

import java.time.Clock;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.command.StartGroupConversationCommand;
import com.architek.oikos.messaging.application.port.in.StartGroupConversationUseCase;
import com.architek.oikos.messaging.application.port.out.UserAccessPort;
import com.architek.oikos.messaging.domain.exception.RecipientNotPropertyMemberException;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Always creates a brand-new GROUP conversation - never a find-or-create:
 * composing to the same set of recipients twice always yields two distinct
 * conversation ids, matching Outlook's "New message" semantics (see
 * Conversation's javadoc). Recipients are validated against the same
 * membership-with-linked-account pool ListRecipientCandidatesService exposes
 * to the recipient picker, via MemberDisplayNameResolver, so the
 * PropertyMemberDirectoryPort/PartyAccountDirectoryPort crossing isn't
 * duplicated here.
 *
 * <p>Creates the conversation envelope AND posts its first message in the
 * same transaction - composing is "send a message", not "start an empty
 * conversation and write into it afterwards" (the same one-step semantics
 * SendBroadcastMessageService already had). A conversation only becomes
 * visibly "a conversation with several replies", in the UI sense the user
 * expects, once a second message is posted into it later via
 * SendMessageService - see Conversation's javadoc.
 */
@Component
public class StartGroupConversationService implements StartGroupConversationUseCase {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserAccessPort userAccessPort;
    private final MemberDisplayNameResolver memberDisplayNameResolver;
    private final SenderIdentityValidator senderIdentityValidator;
    private final Clock clock;

    public StartGroupConversationService(ConversationRepository conversationRepository, MessageRepository messageRepository,
                                          UserAccessPort userAccessPort, MemberDisplayNameResolver memberDisplayNameResolver,
                                          SenderIdentityValidator senderIdentityValidator, Clock clock) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userAccessPort = userAccessPort;
        this.memberDisplayNameResolver = memberDisplayNameResolver;
        this.senderIdentityValidator = senderIdentityValidator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ConversationId start(StartGroupConversationCommand command) {
        if (command.recipientUserIds().isEmpty()) {
            throw new RecipientNotPropertyMemberException("at least one recipient is required");
        }
        if (command.recipientUserIds().contains(command.senderId())) {
            throw new RecipientNotPropertyMemberException("cannot start a conversation with yourself");
        }
        if (!userAccessPort.isMember(command.senderId(), command.propertyId())) {
            throw new RecipientNotPropertyMemberException(
                    "sender is not a member of property " + command.propertyId());
        }
        var senderIdentity = senderIdentityValidator.resolve(command.senderId(), command.propertyId(), command.senderIdentity());

        Map<EntityId, String> membersWithLinkedAccount = memberDisplayNameResolver.namesByUserId(command.propertyId());
        for (EntityId recipientId : command.recipientUserIds()) {
            if (!membersWithLinkedAccount.containsKey(recipientId)) {
                throw new RecipientNotPropertyMemberException(
                        "recipient " + recipientId + " is not a member of property " + command.propertyId()
                                + " with a linked account");
            }
        }

        Set<EntityId> participantUserIds = new HashSet<>(command.recipientUserIds());
        participantUserIds.add(command.senderId());

        Conversation created = Conversation.createGroup(ConversationId.newId(), command.propertyId(), command.senderId(),
                Set.copyOf(participantUserIds), command.subject(), command.concernsUnit());
        ConversationId conversationId = conversationRepository.save(created).getId();

        Message firstMessage = Message.post(MessageId.newId(), conversationId, command.senderId(), senderIdentity,
                command.body(), clock.instant());
        messageRepository.save(firstMessage);

        return conversationId;
    }
}
