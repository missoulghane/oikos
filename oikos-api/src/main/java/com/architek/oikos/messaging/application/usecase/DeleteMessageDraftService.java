package com.architek.oikos.messaging.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.command.DeleteMessageDraftCommand;
import com.architek.oikos.messaging.application.port.in.DeleteMessageDraftUseCase;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;

/** Ownership is already gated upstream (@propertyAccess.isDraftOwner on the endpoint). */
@Component
public class DeleteMessageDraftService implements DeleteMessageDraftUseCase {

    private final MessageDraftRepository messageDraftRepository;

    public DeleteMessageDraftService(MessageDraftRepository messageDraftRepository) {
        this.messageDraftRepository = messageDraftRepository;
    }

    @Override
    @Transactional
    public void delete(DeleteMessageDraftCommand command) {
        messageDraftRepository.deleteById(command.draftId());
    }
}
