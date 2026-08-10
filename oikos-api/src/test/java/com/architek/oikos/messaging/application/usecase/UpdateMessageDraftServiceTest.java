package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.command.SaveMessageDraftCommand;
import com.architek.oikos.messaging.application.command.UpdateMessageDraftCommand;
import com.architek.oikos.messaging.domain.exception.MessageDraftNotFoundException;
import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class UpdateMessageDraftServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-02T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private MessageDraftRepository messageDraftRepository;

    private UpdateMessageDraftService newService() {
        return new UpdateMessageDraftService(messageDraftRepository, CLOCK);
    }

    @Test
    void updates_an_existing_draft_s_content() {
        MessageDraftId id = MessageDraftId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId owner = EntityId.newId();
        MessageDraft existing = MessageDraft.reconstruct(id, propertyId, owner, Set.of(), false, "Old", "Old body",
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T10:00:00Z"));
        when(messageDraftRepository.findById(id)).thenReturn(Optional.of(existing));
        when(messageDraftRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().update(new UpdateMessageDraftCommand(id, new SaveMessageDraftCommand(Set.of(), false, "New", "New body")));

        verify(messageDraftRepository).save(argThat(draft -> draft.getSubject().equals("New")
                && draft.getBody().equals("New body") && draft.getLastModifiedDate().equals(CLOCK.instant())));
    }

    @Test
    void updating_a_missing_draft_is_rejected() {
        MessageDraftId id = MessageDraftId.newId();
        when(messageDraftRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService()
                .update(new UpdateMessageDraftCommand(id, new SaveMessageDraftCommand(Set.of(), false, null, null))))
                .isInstanceOf(MessageDraftNotFoundException.class);
    }
}
