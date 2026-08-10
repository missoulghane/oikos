package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.command.CreateMessageDraftCommand;
import com.architek.oikos.messaging.application.command.SaveMessageDraftCommand;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class CreateMessageDraftServiceTest {

    @Mock
    private MessageDraftRepository messageDraftRepository;

    private CreateMessageDraftService newService() {
        return new CreateMessageDraftService(messageDraftRepository);
    }

    @Test
    void creates_a_draft_with_no_recipients_and_no_content_yet() {
        EntityId propertyId = EntityId.newId();
        EntityId owner = EntityId.newId();
        when(messageDraftRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MessageDraftId id = newService().create(new CreateMessageDraftCommand(propertyId, owner,
                new SaveMessageDraftCommand(Set.of(), false, null, null)));

        assertThat(id).isNotNull();
        verify(messageDraftRepository).save(argThat(draft -> draft.getPropertyId().equals(propertyId)
                && draft.getCreatedBy().equals(owner) && draft.getRecipientUserIds().isEmpty()));
    }

    @Test
    void creates_a_broadcast_draft() {
        EntityId propertyId = EntityId.newId();
        EntityId owner = EntityId.newId();
        when(messageDraftRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().create(new CreateMessageDraftCommand(propertyId, owner,
                new SaveMessageDraftCommand(Set.of(), true, null, "Annonce à tous")));

        verify(messageDraftRepository).save(argThat(draft -> draft.isBroadcast() && draft.getBody().equals("Annonce à tous")));
    }
}
