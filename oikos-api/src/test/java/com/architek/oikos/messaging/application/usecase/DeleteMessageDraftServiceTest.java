package com.architek.oikos.messaging.application.usecase;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.command.DeleteMessageDraftCommand;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;

@ExtendWith(MockitoExtension.class)
class DeleteMessageDraftServiceTest {

    @Mock
    private MessageDraftRepository messageDraftRepository;

    @Test
    void deletes_the_draft_by_id() {
        MessageDraftId id = MessageDraftId.newId();

        new DeleteMessageDraftService(messageDraftRepository).delete(new DeleteMessageDraftCommand(id));

        verify(messageDraftRepository).deleteById(id);
    }
}
