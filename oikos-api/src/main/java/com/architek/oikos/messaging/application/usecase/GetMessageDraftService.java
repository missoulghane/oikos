package com.architek.oikos.messaging.application.usecase;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.dto.ConversationParticipantView;
import com.architek.oikos.messaging.application.dto.MessageDraftView;
import com.architek.oikos.messaging.application.port.in.GetMessageDraftUseCase;
import com.architek.oikos.messaging.application.query.GetMessageDraftQuery;
import com.architek.oikos.messaging.domain.exception.MessageDraftNotFoundException;
import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class GetMessageDraftService implements GetMessageDraftUseCase {

    private final MessageDraftRepository messageDraftRepository;
    private final MemberDisplayNameResolver memberDisplayNameResolver;

    public GetMessageDraftService(MessageDraftRepository messageDraftRepository,
                                   MemberDisplayNameResolver memberDisplayNameResolver) {
        this.messageDraftRepository = messageDraftRepository;
        this.memberDisplayNameResolver = memberDisplayNameResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public MessageDraftView getDraft(GetMessageDraftQuery query) {
        MessageDraft draft = messageDraftRepository.findById(query.draftId())
                .orElseThrow(() -> new MessageDraftNotFoundException(query.draftId()));

        Map<EntityId, String> memberNames = memberDisplayNameResolver.namesByUserId(draft.getPropertyId());
        var recipients = draft.getRecipientUserIds().stream()
                .map(recipientId -> new ConversationParticipantView(recipientId, memberNames.get(recipientId)))
                .toList();

        return new MessageDraftView(draft.getId(), draft.getPropertyId(), draft.getCreatedBy(), recipients,
                draft.isBroadcast(), draft.getSubject(), draft.getBody());
    }
}
