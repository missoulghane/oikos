package com.architek.oikos.messaging.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.messaging.application.dto.ConversationParticipantView;
import com.architek.oikos.messaging.application.dto.ConversationSummaryView;
import com.architek.oikos.messaging.application.port.out.PropertyMemberDirectoryPort;
import com.architek.oikos.messaging.application.port.out.UserAccessPort;
import com.architek.oikos.messaging.application.query.ConversationBox;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.ConversationReadMarker;
import com.architek.oikos.messaging.domain.model.ConversationType;
import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.repository.ConversationReadMarkerRepository;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Shared aggregation logic behind both ListMyConversationsService and
 * GetUnreadSummaryService: the caller's GROUP conversations (wherever they
 * are a participant) plus the BROADCAST channel of every property they are
 * currently a member of (visible even if never opened before, same pattern
 * as GetMyInstallmentsService's cross-property aggregation). Everything is
 * resolved and sorted in memory - the same "small enough to hold in memory"
 * assumption used throughout the app for per-caller cross-property lists
 * (ListContactsByPropertyService, GetMyInstallmentsService).
 */
@Component
class ConversationAggregator {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationReadMarkerRepository readMarkerRepository;
    private final UserAccessPort userAccessPort;
    private final PropertyMemberDirectoryPort propertyMemberDirectoryPort;
    private final MemberDisplayNameResolver memberDisplayNameResolver;

    ConversationAggregator(ConversationRepository conversationRepository, MessageRepository messageRepository,
                            ConversationReadMarkerRepository readMarkerRepository, UserAccessPort userAccessPort,
                            PropertyMemberDirectoryPort propertyMemberDirectoryPort,
                            MemberDisplayNameResolver memberDisplayNameResolver) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.readMarkerRepository = readMarkerRepository;
        this.userAccessPort = userAccessPort;
        this.propertyMemberDirectoryPort = propertyMemberDirectoryPort;
        this.memberDisplayNameResolver = memberDisplayNameResolver;
    }

    /** Sorted by lastMessageAt descending, conversations with no message yet sorted last. Unfiltered by box. */
    List<ConversationSummaryView> listAll(EntityId userId, String search) {
        return listAll(userId, search, null);
    }

    /** Same as {@link #listAll(EntityId, String)}, additionally restricted to conversations the
     * caller has received into (box == RECEIVED) or sent into (box == SENT) at least one message;
     * box == null means unfiltered. */
    List<ConversationSummaryView> listAll(EntityId userId, String search, ConversationBox box) {
        List<Conversation> groups = conversationRepository.findAllGroupByParticipant(userId);
        List<Conversation> broadcasts = conversationRepository.findAllBroadcastByPropertyIds(userAccessPort.memberPropertyIds(userId));

        List<Conversation> all = new ArrayList<>(groups);
        all.addAll(broadcasts);

        Map<EntityId, String> propertyNameCache = new HashMap<>();
        Map<EntityId, Map<EntityId, String>> memberNamesByPropertyCache = new HashMap<>();

        List<ConversationSummaryView> views = new ArrayList<>();
        for (Conversation conversation : all) {
            EntityId propertyId = conversation.getPropertyId();
            String propertyName = propertyNameCache.computeIfAbsent(propertyId, propertyMemberDirectoryPort::getPropertyName);

            List<ConversationParticipantView> participants = List.of();
            if (conversation.getType() == ConversationType.GROUP) {
                Map<EntityId, String> memberNames =
                        memberNamesByPropertyCache.computeIfAbsent(propertyId, memberDisplayNameResolver::namesByUserId);
                participants = conversation.getParticipantUserIds().stream()
                        .filter(participantId -> !participantId.equals(userId))
                        .map(participantId -> new ConversationParticipantView(participantId, memberNames.get(participantId)))
                        .sorted(Comparator.comparing(ConversationParticipantView::fullName,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                        .toList();
            }

            Optional<Message> lastMessage = messageRepository.findLastMessage(conversation.getId());
            String preview = lastMessage.map(message -> message.getBody().value()).orElse(null);
            Instant lastMessageAt = lastMessage.map(Message::getCreatedDate).orElse(null);
            long messageCount = messageRepository.countByConversation(conversation.getId());

            if (box != null) {
                long sentByMe = messageRepository.countByConversationAndSender(conversation.getId(), userId);
                boolean matchesBox = box == ConversationBox.SENT ? sentByMe > 0 : messageCount > sentByMe;
                if (!matchesBox) {
                    continue;
                }
            }

            MessageId lastReadMessageId = readMarkerRepository.findByConversationIdAndUserId(conversation.getId(), userId)
                    .map(ConversationReadMarker::getLastReadMessageId)
                    .orElse(null);
            long unreadCount = messageRepository.countUnread(conversation.getId(), lastReadMessageId);

            String subject = conversation.getSubject() != null ? conversation.getSubject().value() : null;
            views.add(new ConversationSummaryView(conversation.getId(), conversation.getType(), propertyId, propertyName,
                    subject, participants, preview, lastMessageAt, unreadCount, messageCount));
        }

        return views.stream()
                .filter(view -> matchesSearch(view, search))
                .sorted(Comparator.comparing(ConversationSummaryView::lastMessageAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private static boolean matchesSearch(ConversationSummaryView view, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String pattern = search.trim().toLowerCase(Locale.ROOT);
        boolean matchesSubject = view.subject() != null && view.subject().toLowerCase(Locale.ROOT).contains(pattern);
        boolean matchesAnyParticipant = view.participants().stream()
                .anyMatch(participant -> participant.fullName() != null && participant.fullName().toLowerCase(Locale.ROOT).contains(pattern));
        boolean matchesPropertyName = view.propertyName() != null && view.propertyName().toLowerCase(Locale.ROOT).contains(pattern);
        return matchesSubject || matchesAnyParticipant || matchesPropertyName;
    }
}
