package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.command.SendMessageDraftCommand;
import com.architek.oikos.messaging.application.port.in.SendBroadcastMessageUseCase;
import com.architek.oikos.messaging.application.port.in.StartGroupConversationUseCase;
import com.architek.oikos.messaging.application.port.out.UserAccessPort;
import com.architek.oikos.messaging.domain.exception.MessageDraftNotFoundException;
import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.UnauthorizedException;

@ExtendWith(MockitoExtension.class)
class SendMessageDraftServiceTest {

    @Mock
    private MessageDraftRepository messageDraftRepository;

    @Mock
    private StartGroupConversationUseCase startGroupConversationUseCase;

    @Mock
    private SendBroadcastMessageUseCase sendBroadcastMessageUseCase;

    @Mock
    private UserAccessPort userAccessPort;

    private SendMessageDraftService newService() {
        return new SendMessageDraftService(messageDraftRepository, startGroupConversationUseCase,
                sendBroadcastMessageUseCase, userAccessPort);
    }

    @Test
    void sending_a_group_draft_starts_a_group_conversation_and_deletes_the_draft() {
        MessageDraftId draftId = MessageDraftId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId owner = EntityId.newId();
        EntityId recipient = EntityId.newId();
        MessageDraft draft = MessageDraft.create(draftId, propertyId, owner, Set.of(recipient), false, "Sujet", "Corps");
        when(messageDraftRepository.findById(draftId)).thenReturn(Optional.of(draft));
        ConversationId conversationId = ConversationId.newId();
        when(startGroupConversationUseCase.start(any())).thenReturn(conversationId);

        ConversationId result = newService().send(new SendMessageDraftCommand(draftId));

        assertThat(result).isEqualTo(conversationId);
        verify(startGroupConversationUseCase).start(argThat(command -> command.propertyId().equals(propertyId)
                && command.senderId().equals(owner) && command.recipientUserIds().equals(Set.of(recipient))
                && command.subject().value().equals("Sujet") && command.body().value().equals("Corps")));
        verify(messageDraftRepository).deleteById(draftId);
    }

    @Test
    void sending_a_broadcast_draft_the_owner_still_may_broadcast_sends_it_and_deletes_the_draft() {
        MessageDraftId draftId = MessageDraftId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId owner = EntityId.newId();
        // Un brouillon d'envoi groupé porte son objet comme les autres depuis
        // que le canal unique a disparu.
        MessageDraft draft = MessageDraft.create(draftId, propertyId, owner, Set.of(), true, "Coupure d'eau", "Annonce");
        when(messageDraftRepository.findById(draftId)).thenReturn(Optional.of(draft));
        when(userAccessPort.canBroadcast(owner, propertyId)).thenReturn(true);
        ConversationId conversationId = ConversationId.newId();
        when(sendBroadcastMessageUseCase.send(any())).thenReturn(conversationId);

        ConversationId result = newService().send(new SendMessageDraftCommand(draftId));

        assertThat(result).isEqualTo(conversationId);
        verify(sendBroadcastMessageUseCase).send(argThat(command -> command.propertyId().equals(propertyId)
                && command.senderId().equals(owner) && command.subject().value().equals("Coupure d'eau")
                && command.body().value().equals("Annonce")));
        verify(messageDraftRepository).deleteById(draftId);
    }

    @Test
    void sending_a_broadcast_draft_after_losing_broadcast_permission_is_rejected_and_the_draft_is_kept() {
        MessageDraftId draftId = MessageDraftId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId owner = EntityId.newId();
        MessageDraft draft = MessageDraft.create(draftId, propertyId, owner, Set.of(), true, null, "Annonce");
        when(messageDraftRepository.findById(draftId)).thenReturn(Optional.of(draft));
        when(userAccessPort.canBroadcast(owner, propertyId)).thenReturn(false);

        assertThatThrownBy(() -> newService().send(new SendMessageDraftCommand(draftId)))
                .isInstanceOf(UnauthorizedException.class);

        verify(sendBroadcastMessageUseCase, never()).send(any());
        verify(messageDraftRepository, never()).deleteById(any());
    }

    @Test
    void sending_a_draft_with_no_body_yet_is_rejected_as_a_validation_error_not_a_null_pointer() {
        MessageDraftId draftId = MessageDraftId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId owner = EntityId.newId();
        EntityId recipient = EntityId.newId();
        MessageDraft draft = MessageDraft.create(draftId, propertyId, owner, Set.of(recipient), false, "Sujet", null);
        when(messageDraftRepository.findById(draftId)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> newService().send(new SendMessageDraftCommand(draftId)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(messageDraftRepository, never()).deleteById(any());
    }

    @Test
    void sending_a_missing_draft_is_rejected() {
        MessageDraftId draftId = MessageDraftId.newId();
        when(messageDraftRepository.findById(draftId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().send(new SendMessageDraftCommand(draftId)))
                .isInstanceOf(MessageDraftNotFoundException.class);
    }
}
