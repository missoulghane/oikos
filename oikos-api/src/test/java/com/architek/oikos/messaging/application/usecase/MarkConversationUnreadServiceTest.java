package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.command.MarkConversationUnreadCommand;
import com.architek.oikos.messaging.domain.model.ConversationReadMarker;
import com.architek.oikos.messaging.domain.repository.ConversationReadMarkerRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class MarkConversationUnreadServiceTest {

    @Mock
    private ConversationReadMarkerRepository readMarkerRepository;

    private MarkConversationUnreadService newService() {
        return new MarkConversationUnreadService(readMarkerRepository);
    }

    // Le curseur revient a zero : tout recompte comme non lu, exactement comme
    // une conversation jamais ouverte.
    @Test
    void marking_unread_clears_the_read_cursor() {
        ConversationId conversationId = ConversationId.newId();
        EntityId userId = EntityId.newId();

        newService().markUnread(new MarkConversationUnreadCommand(conversationId, userId));

        ArgumentCaptor<ConversationReadMarker> captor = ArgumentCaptor.forClass(ConversationReadMarker.class);
        verify(readMarkerRepository).save(captor.capture());
        assertThat(captor.getValue().getConversationId()).isEqualTo(conversationId);
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getLastReadMessageId()).isNull();
        assertThat(captor.getValue().getLastReadAt()).isNull();
    }
}
