package com.architek.oikos.messaging.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.dto.ConversationView;
import com.architek.oikos.messaging.application.port.in.GetConversationUseCase;
import com.architek.oikos.messaging.application.query.GetConversationQuery;
import com.architek.oikos.messaging.domain.exception.ConversationNotFoundException;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;

@Component
public class GetConversationService implements GetConversationUseCase {

    private final ConversationRepository conversationRepository;

    public GetConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationView getConversation(GetConversationQuery query) {
        return conversationRepository.findById(query.conversationId())
                .map(ConversationView::from)
                .orElseThrow(() -> new ConversationNotFoundException(query.conversationId()));
    }
}
