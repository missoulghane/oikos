package com.architek.oikos.messaging.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.command.SendBroadcastMessageCommand;
import com.architek.oikos.messaging.application.command.SendMessageDraftCommand;
import com.architek.oikos.messaging.application.command.StartGroupConversationCommand;
import com.architek.oikos.messaging.application.port.in.SendBroadcastMessageUseCase;
import com.architek.oikos.messaging.application.port.in.SendMessageDraftUseCase;
import com.architek.oikos.messaging.application.port.in.StartGroupConversationUseCase;
import com.architek.oikos.messaging.application.port.out.UserAccessPort;
import com.architek.oikos.messaging.domain.exception.MessageDraftNotFoundException;
import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.shared.exception.UnauthorizedException;

/**
 * Reuses StartGroupConversationService/SendBroadcastMessageService entirely
 * rather than duplicating their validation (recipients still property
 * members, non-blank subject/body) - this service's only extra
 * responsibility is the broadcast-permission re-check below and deleting the
 * draft once it has become a real conversation.
 *
 * <p>The draft-send endpoint's @PreAuthorize only proves draft ownership
 * (@propertyAccess.isDraftOwner), not current broadcast rights - unlike
 * POST /properties/{id}/broadcast-messages, whose @PreAuthorize
 * (canBroadcastOnProperty) is the sole gate SendBroadcastMessageService
 * trusts. A broadcast draft's owner could have lost Permission.
 * MESSAGING_BROADCAST since saving it (e.g. demoted off the board), so that
 * check has to be repeated here before delegating.
 */
@Component
public class SendMessageDraftService implements SendMessageDraftUseCase {

    private final MessageDraftRepository messageDraftRepository;
    private final StartGroupConversationUseCase startGroupConversationUseCase;
    private final SendBroadcastMessageUseCase sendBroadcastMessageUseCase;
    private final UserAccessPort userAccessPort;

    public SendMessageDraftService(MessageDraftRepository messageDraftRepository,
                                    StartGroupConversationUseCase startGroupConversationUseCase,
                                    SendBroadcastMessageUseCase sendBroadcastMessageUseCase,
                                    UserAccessPort userAccessPort) {
        this.messageDraftRepository = messageDraftRepository;
        this.startGroupConversationUseCase = startGroupConversationUseCase;
        this.sendBroadcastMessageUseCase = sendBroadcastMessageUseCase;
        this.userAccessPort = userAccessPort;
    }

    @Override
    @Transactional
    public ConversationId send(SendMessageDraftCommand command) {
        MessageDraft draft = messageDraftRepository.findById(command.draftId())
                .orElseThrow(() -> new MessageDraftNotFoundException(command.draftId()));

        ConversationId conversationId = draft.isBroadcast() ? sendBroadcast(draft) : startGroupConversation(draft);

        messageDraftRepository.deleteById(draft.getId());
        return conversationId;
    }

    private ConversationId sendBroadcast(MessageDraft draft) {
        if (!userAccessPort.canBroadcast(draft.getCreatedBy(), draft.getPropertyId())) {
            throw new UnauthorizedException("no longer allowed to broadcast on property " + draft.getPropertyId());
        }
        return sendBroadcastMessageUseCase.send(
                new SendBroadcastMessageCommand(draft.getPropertyId(), draft.getCreatedBy(), messageBody(draft)));
    }

    private ConversationId startGroupConversation(MessageDraft draft) {
        return startGroupConversationUseCase.start(new StartGroupConversationCommand(draft.getPropertyId(),
                draft.getCreatedBy(), draft.getRecipientUserIds(), conversationSubject(draft), messageBody(draft)));
    }

    // A still-null subject/body (never filled in) must surface as the same 400 IllegalArgumentException
    // as a blank one, not an unrelated NullPointerException (500) from these value objects' requireNonNull.
    private static MessageBody messageBody(MessageDraft draft) {
        return MessageBody.of(draft.getBody() != null ? draft.getBody() : "");
    }

    private static ConversationSubject conversationSubject(MessageDraft draft) {
        return ConversationSubject.of(draft.getSubject() != null ? draft.getSubject() : "");
    }
}
