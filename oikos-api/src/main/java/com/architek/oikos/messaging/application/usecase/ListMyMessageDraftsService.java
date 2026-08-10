package com.architek.oikos.messaging.application.usecase;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.dto.ConversationParticipantView;
import com.architek.oikos.messaging.application.dto.MessageDraftSummaryView;
import com.architek.oikos.messaging.application.port.in.ListMyMessageDraftsUseCase;
import com.architek.oikos.messaging.application.port.out.PropertyMemberDirectoryPort;
import com.architek.oikos.messaging.application.query.ListMyMessageDraftsQuery;
import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** Real DB-level pagination (see MessageDraftRepository.findByCreatedBy) - unlike
 * ListMyConversationsService/ConversationAggregator's in-memory approach, a draft has no
 * cross-source (GROUP+BROADCAST) aggregation to do, so the repository already returns exactly
 * one page. Recipient/property names are still resolved per row here (same
 * MemberDisplayNameResolver/PropertyMemberDirectoryPort ConversationAggregator uses) so the
 * frontend never needs a second round trip to render a draft list row. */
@Component
public class ListMyMessageDraftsService implements ListMyMessageDraftsUseCase {

    private final MessageDraftRepository messageDraftRepository;
    private final PropertyMemberDirectoryPort propertyMemberDirectoryPort;
    private final MemberDisplayNameResolver memberDisplayNameResolver;

    public ListMyMessageDraftsService(MessageDraftRepository messageDraftRepository,
                                       PropertyMemberDirectoryPort propertyMemberDirectoryPort,
                                       MemberDisplayNameResolver memberDisplayNameResolver) {
        this.messageDraftRepository = messageDraftRepository;
        this.propertyMemberDirectoryPort = propertyMemberDirectoryPort;
        this.memberDisplayNameResolver = memberDisplayNameResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDraftSummaryView> listDrafts(ListMyMessageDraftsQuery query) {
        Page<MessageDraft> page = messageDraftRepository.findByCreatedBy(query.userId(), query.pageRequest(), query.search());

        Map<EntityId, String> propertyNameCache = new HashMap<>();
        Map<EntityId, Map<EntityId, String>> memberNamesByPropertyCache = new HashMap<>();

        List<MessageDraftSummaryView> content = page.content().stream().map(draft -> {
            EntityId propertyId = draft.getPropertyId();
            String propertyName = propertyNameCache.computeIfAbsent(propertyId, propertyMemberDirectoryPort::getPropertyName);
            Map<EntityId, String> memberNames =
                    memberNamesByPropertyCache.computeIfAbsent(propertyId, memberDisplayNameResolver::namesByUserId);
            List<ConversationParticipantView> recipients = draft.getRecipientUserIds().stream()
                    .map(recipientId -> new ConversationParticipantView(recipientId, memberNames.get(recipientId)))
                    .sorted(Comparator.comparing(ConversationParticipantView::fullName,
                            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .toList();
            return new MessageDraftSummaryView(draft.getId(), propertyId, propertyName, draft.isBroadcast(), recipients,
                    draft.getSubject(), draft.getBody(), draft.getLastModifiedDate());
        }).toList();

        return Page.of(content, page.pageNumber(), page.pageSize(), page.totalElements());
    }
}
