package com.architek.oikos.messaging.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.command.CreateMessageDraftCommand;
import com.architek.oikos.messaging.application.port.in.CreateMessageDraftUseCase;
import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;

@Component
public class CreateMessageDraftService implements CreateMessageDraftUseCase {

    private final MessageDraftRepository messageDraftRepository;

    public CreateMessageDraftService(MessageDraftRepository messageDraftRepository) {
        this.messageDraftRepository = messageDraftRepository;
    }

    @Override
    @Transactional
    public MessageDraftId create(CreateMessageDraftCommand command) {
        MessageDraft draft = MessageDraft.create(MessageDraftId.newId(), command.propertyId(), command.ownerId(),
                command.payload().recipientUserIds(), command.payload().broadcast(), command.payload().subject(),
                command.payload().body());
        return messageDraftRepository.save(draft).getId();
    }
}
