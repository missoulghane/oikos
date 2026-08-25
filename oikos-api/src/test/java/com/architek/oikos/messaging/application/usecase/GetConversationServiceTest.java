package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.dto.ConversationView;
import com.architek.oikos.messaging.application.query.GetConversationQuery;
import com.architek.oikos.messaging.domain.exception.ConversationNotFoundException;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    private GetConversationService newService() {
        return new GetConversationService(conversationRepository);
    }

    @Test
    void returns_the_conversation_view_when_found() {
        ConversationId id = ConversationId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId creator = EntityId.newId();
        Conversation conversation = Conversation.createBroadcast(id, propertyId, creator, ConversationSubject.of("Annonce"));
        when(conversationRepository.findById(id)).thenReturn(Optional.of(conversation));

        ConversationView view = newService().getConversation(new GetConversationQuery(id));

        assertThat(view.id()).isEqualTo(id);
        assertThat(view.propertyId()).isEqualTo(propertyId);
    }

    @Test
    void throws_when_the_conversation_does_not_exist() {
        ConversationId id = ConversationId.newId();
        when(conversationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getConversation(new GetConversationQuery(id)))
                .isInstanceOf(ConversationNotFoundException.class);
    }
}
