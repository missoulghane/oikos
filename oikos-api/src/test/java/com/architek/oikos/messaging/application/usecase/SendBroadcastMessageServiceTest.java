package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.command.SendBroadcastMessageCommand;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class SendBroadcastMessageServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    private SendBroadcastMessageService newService() {
        return new SendBroadcastMessageService(conversationRepository, messageRepository, CLOCK);
    }

    @Test
    void broadcasting_for_the_first_time_creates_the_property_s_channel() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        when(conversationRepository.findBroadcastConversation(propertyId)).thenReturn(Optional.empty());
        when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationId id = newService().send(new SendBroadcastMessageCommand(propertyId, sender, MessageBody.of("Annonce")));

        assertThat(id).isNotNull();
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void broadcasting_again_reuses_the_existing_channel() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        Conversation existing = Conversation.createBroadcast(ConversationId.newId(), propertyId, sender);
        when(conversationRepository.findBroadcastConversation(propertyId)).thenReturn(Optional.of(existing));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationId id = newService().send(new SendBroadcastMessageCommand(propertyId, sender, MessageBody.of("Annonce 2")));

        assertThat(id).isEqualTo(existing.getId());
        verify(conversationRepository, never()).save(any());
    }
}
