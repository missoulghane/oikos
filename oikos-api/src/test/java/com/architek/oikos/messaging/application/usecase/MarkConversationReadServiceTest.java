package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.command.MarkConversationReadCommand;
import com.architek.oikos.messaging.domain.model.ConversationReadMarker;
import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.repository.ConversationReadMarkerRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class MarkConversationReadServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationReadMarkerRepository readMarkerRepository;

    private MarkConversationReadService newService() {
        return new MarkConversationReadService(messageRepository, readMarkerRepository, CLOCK);
    }

    @Test
    void marking_read_for_the_first_time_creates_a_new_marker_pointing_at_the_last_message() {
        ConversationId conversationId = ConversationId.newId();
        EntityId userId = EntityId.newId();
        Message lastMessage = Message.post(MessageId.newId(), conversationId, EntityId.newId(), MessageBody.of("Hi"), Instant.EPOCH);
        when(messageRepository.findLastMessage(conversationId)).thenReturn(Optional.of(lastMessage));
        when(readMarkerRepository.findByConversationIdAndUserId(conversationId, userId)).thenReturn(Optional.empty());
        when(readMarkerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().markRead(new MarkConversationReadCommand(conversationId, userId));

        ArgumentCaptor<ConversationReadMarker> captor = ArgumentCaptor.forClass(ConversationReadMarker.class);
        verify(readMarkerRepository).save(captor.capture());
        assertThat(captor.getValue().getLastReadMessageId()).isEqualTo(lastMessage.getId());
        assertThat(captor.getValue().getLastReadAt()).isEqualTo(CLOCK.instant());
    }

    @Test
    void marking_read_a_conversation_with_no_message_yet_stores_a_null_last_read_message() {
        ConversationId conversationId = ConversationId.newId();
        EntityId userId = EntityId.newId();
        when(messageRepository.findLastMessage(conversationId)).thenReturn(Optional.empty());
        when(readMarkerRepository.findByConversationIdAndUserId(conversationId, userId)).thenReturn(Optional.empty());
        when(readMarkerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().markRead(new MarkConversationReadCommand(conversationId, userId));

        ArgumentCaptor<ConversationReadMarker> captor = ArgumentCaptor.forClass(ConversationReadMarker.class);
        verify(readMarkerRepository).save(captor.capture());
        assertThat(captor.getValue().getLastReadMessageId()).isNull();
        assertThat(captor.getValue().getLastReadAt()).isEqualTo(CLOCK.instant());
    }

    @Test
    void marking_read_again_updates_the_existing_marker() {
        ConversationId conversationId = ConversationId.newId();
        EntityId userId = EntityId.newId();
        ConversationReadMarker existing = ConversationReadMarker.unread(conversationId, userId);
        Message lastMessage = Message.post(MessageId.newId(), conversationId, EntityId.newId(), MessageBody.of("Hi"), Instant.EPOCH);
        when(messageRepository.findLastMessage(conversationId)).thenReturn(Optional.of(lastMessage));
        when(readMarkerRepository.findByConversationIdAndUserId(conversationId, userId)).thenReturn(Optional.of(existing));
        when(readMarkerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().markRead(new MarkConversationReadCommand(conversationId, userId));

        verify(readMarkerRepository).save(any());
    }
}
