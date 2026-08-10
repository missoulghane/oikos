package com.architek.oikos.messaging.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.command.MarkConversationReadCommand;
import com.architek.oikos.messaging.application.port.in.MarkConversationReadUseCase;
import com.architek.oikos.messaging.domain.model.ConversationReadMarker;
import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.repository.ConversationReadMarkerRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;

/**
 * Upsert-lazy: the read marker row is created on first access (including for
 * a BROADCAST channel never opened before), never seeded upfront. If the
 * conversation has no message yet, lastReadMessageId stays null - the same
 * "never read" representation as a marker that was never created at all
 * (see ConversationReadMarker.unread), so ListMyConversationsService's
 * unread-count logic doesn't need to special-case it.
 */
@Component
public class MarkConversationReadService implements MarkConversationReadUseCase {

    private final MessageRepository messageRepository;
    private final ConversationReadMarkerRepository readMarkerRepository;
    private final Clock clock;

    public MarkConversationReadService(MessageRepository messageRepository, ConversationReadMarkerRepository readMarkerRepository,
                                        Clock clock) {
        this.messageRepository = messageRepository;
        this.readMarkerRepository = readMarkerRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void markRead(MarkConversationReadCommand command) {
        var lastMessageId = messageRepository.findLastMessage(command.conversationId()).map(Message::getId).orElse(null);

        ConversationReadMarker marker = readMarkerRepository.findByConversationIdAndUserId(command.conversationId(), command.userId())
                .orElseGet(() -> ConversationReadMarker.unread(command.conversationId(), command.userId()));

        readMarkerRepository.save(marker.markRead(lastMessageId, clock.instant()));
    }
}
