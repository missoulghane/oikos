package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.dto.MessageView;
import com.architek.oikos.messaging.application.query.ListConversationMessagesQuery;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListConversationMessagesServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MemberDisplayNameResolver memberDisplayNameResolver;

    private ListConversationMessagesService newService() {
        return new ListConversationMessagesService(conversationRepository, messageRepository, memberDisplayNameResolver);
    }

    @Test
    void resolves_sender_names_and_flags_the_caller_s_own_messages_as_mine() {
        EntityId propertyId = EntityId.newId();
        EntityId caller = EntityId.newId();
        EntityId other = EntityId.newId();
        ConversationId conversationId = ConversationId.newId();
        Conversation conversation = Conversation.createGroup(conversationId, propertyId, caller, Set.of(caller, other),
                ConversationSubject.of("Sujet"), null);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        Message fromCaller = Message.post(MessageId.newId(), conversationId, caller, SenderIdentity.OWNER, MessageBody.of("Hi"),
                Instant.EPOCH);
        Message fromOther = Message.post(MessageId.newId(), conversationId, other, SenderIdentity.OWNER, MessageBody.of("Hello"),
                Instant.EPOCH.plusSeconds(1));
        when(messageRepository.findRecentPage(conversationId, PageRequest.of(0, 50)))
                .thenReturn(Page.of(java.util.List.of(fromCaller, fromOther), 0, 50, 2));
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(caller, "Caller Name", other, "Other Name"));

        Page<MessageView> page = newService().listMessages(new ListConversationMessagesQuery(conversationId, caller, PageRequest.of(0, 50)));

        assertThat(page.content()).hasSize(2);
        assertThat(page.content().get(0).senderName()).isEqualTo("Caller Name");
        assertThat(page.content().get(0).mine()).isTrue();
        assertThat(page.content().get(1).senderName()).isEqualTo("Other Name");
        assertThat(page.content().get(1).mine()).isFalse();
    }
}
