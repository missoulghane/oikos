package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.dto.MessageDraftView;
import com.architek.oikos.messaging.application.query.GetMessageDraftQuery;
import com.architek.oikos.messaging.domain.exception.MessageDraftNotFoundException;
import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetMessageDraftServiceTest {

    @Mock
    private MessageDraftRepository messageDraftRepository;

    @Mock
    private MemberDisplayNameResolver memberDisplayNameResolver;

    private GetMessageDraftService newService() {
        return new GetMessageDraftService(messageDraftRepository, memberDisplayNameResolver);
    }

    @Test
    void gets_a_draft_with_its_recipients_display_names_resolved() {
        MessageDraftId id = MessageDraftId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId owner = EntityId.newId();
        EntityId recipient = EntityId.newId();
        MessageDraft draft = MessageDraft.create(id, propertyId, owner, Set.of(recipient), false, "Sujet", "Corps");
        when(messageDraftRepository.findById(id)).thenReturn(Optional.of(draft));
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(recipient, "Jean Dupont"));

        MessageDraftView view = newService().getDraft(new GetMessageDraftQuery(id));

        assertThat(view.createdBy()).isEqualTo(owner);
        assertThat(view.recipients()).extracting(participant -> participant.fullName()).containsExactly("Jean Dupont");
    }

    @Test
    void getting_a_missing_draft_is_rejected() {
        MessageDraftId id = MessageDraftId.newId();
        when(messageDraftRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getDraft(new GetMessageDraftQuery(id)))
                .isInstanceOf(MessageDraftNotFoundException.class);
    }
}
