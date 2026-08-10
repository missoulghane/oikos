package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.dto.MessageDraftSummaryView;
import com.architek.oikos.messaging.application.port.out.PropertyMemberDirectoryPort;
import com.architek.oikos.messaging.application.query.ListMyMessageDraftsQuery;
import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListMyMessageDraftsServiceTest {

    @Mock
    private MessageDraftRepository messageDraftRepository;

    @Mock
    private PropertyMemberDirectoryPort propertyMemberDirectoryPort;

    @Mock
    private MemberDisplayNameResolver memberDisplayNameResolver;

    private ListMyMessageDraftsService newService() {
        return new ListMyMessageDraftsService(messageDraftRepository, propertyMemberDirectoryPort, memberDisplayNameResolver);
    }

    @Test
    void lists_the_caller_s_drafts_with_property_name_and_recipient_names_resolved() {
        EntityId owner = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId recipient = EntityId.newId();
        MessageDraft draft = MessageDraft.create(MessageDraftId.newId(), propertyId, owner, Set.of(recipient), false,
                "Sujet", "Corps");
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(messageDraftRepository.findByCreatedBy(owner, pageRequest, null))
                .thenReturn(Page.of(List.of(draft), 0, 20, 1));
        when(propertyMemberDirectoryPort.getPropertyName(propertyId)).thenReturn("Résidence Alpha");
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(recipient, "Jean Dupont"));

        Page<MessageDraftSummaryView> page = newService().listDrafts(new ListMyMessageDraftsQuery(owner, pageRequest, null));

        assertThat(page.content()).hasSize(1);
        MessageDraftSummaryView view = page.content().get(0);
        assertThat(view.propertyName()).isEqualTo("Résidence Alpha");
        assertThat(view.recipients()).extracting(participant -> participant.fullName()).containsExactly("Jean Dupont");
        assertThat(view.subject()).isEqualTo("Sujet");
    }

    @Test
    void a_broadcast_draft_has_no_resolved_recipients() {
        EntityId owner = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        MessageDraft draft = MessageDraft.create(MessageDraftId.newId(), propertyId, owner, Set.of(), true, null, "Annonce");
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(messageDraftRepository.findByCreatedBy(owner, pageRequest, null))
                .thenReturn(Page.of(List.of(draft), 0, 20, 1));
        when(propertyMemberDirectoryPort.getPropertyName(propertyId)).thenReturn("Résidence Alpha");
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of());

        Page<MessageDraftSummaryView> page = newService().listDrafts(new ListMyMessageDraftsQuery(owner, pageRequest, null));

        assertThat(page.content().get(0).broadcast()).isTrue();
        assertThat(page.content().get(0).recipients()).isEmpty();
    }
}
