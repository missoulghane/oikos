package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.command.SendBroadcastMessageCommand;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
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
    void a_broadcast_carries_its_own_subject() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationId id = newService().send(new SendBroadcastMessageCommand(propertyId, sender,
                ConversationSubject.of("Coupure d'eau jeudi"), MessageBody.of("Annonce")));

        assertThat(id).isNotNull();
        ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(captor.capture());
        assertThat(captor.getValue().getSubject().value()).isEqualTo("Coupure d'eau jeudi");
    }

    // Le fond de la demande : deux annonces sans rapport ne sont pas un fil.
    // Elles se retrouvaient bout à bout dans l'unique canal de la copropriété.
    @Test
    void broadcasting_again_starts_a_new_conversation_rather_than_reusing_one() {
        EntityId propertyId = EntityId.newId();
        EntityId sender = EntityId.newId();
        when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationId first = newService().send(new SendBroadcastMessageCommand(propertyId, sender,
                ConversationSubject.of("Coupure d'eau"), MessageBody.of("Annonce 1")));
        ConversationId second = newService().send(new SendBroadcastMessageCommand(propertyId, sender,
                ConversationSubject.of("Ravalement"), MessageBody.of("Annonce 2")));

        assertThat(first).isNotEqualTo(second);
        verify(conversationRepository, times(2)).save(any(Conversation.class));
    }
}
