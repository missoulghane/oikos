package com.architek.oikos.messaging.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.command.UpdateMessageDraftCommand;
import com.architek.oikos.messaging.application.port.in.UpdateMessageDraftUseCase;
import com.architek.oikos.messaging.domain.exception.MessageDraftNotFoundException;
import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;

/** Ownership is already gated upstream (@propertyAccess.isDraftOwner on the endpoint) - this
 * service trusts its caller, same trust boundary as SendBroadcastMessageService. */
@Component
public class UpdateMessageDraftService implements UpdateMessageDraftUseCase {

    private final MessageDraftRepository messageDraftRepository;
    private final Clock clock;

    public UpdateMessageDraftService(MessageDraftRepository messageDraftRepository, Clock clock) {
        this.messageDraftRepository = messageDraftRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void update(UpdateMessageDraftCommand command) {
        MessageDraft draft = messageDraftRepository.findById(command.draftId())
                .orElseThrow(() -> new MessageDraftNotFoundException(command.draftId()));
        MessageDraft updated = draft.update(command.payload().recipientUserIds(), command.payload().broadcast(),
                command.payload().subject(), command.payload().body(), clock.instant());
        messageDraftRepository.save(updated);
    }
}
