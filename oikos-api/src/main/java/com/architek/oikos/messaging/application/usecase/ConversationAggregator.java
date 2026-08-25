package com.architek.oikos.messaging.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.architek.oikos.messaging.application.dto.ConversationParticipantView;
import com.architek.oikos.messaging.application.dto.ConversationSummaryView;
import com.architek.oikos.messaging.application.port.out.PropertyMemberDirectoryPort;
import com.architek.oikos.messaging.application.port.out.UserAccessPort;
import com.architek.oikos.messaging.application.query.ConversationBox;
import com.architek.oikos.messaging.application.query.ConversationReadState;
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
        return listAll(userId, search, null, null);
    }

    /** Same as {@link #listAll(EntityId, String)}, additionally restricted to conversations the
     * caller has received into (box == RECEIVED) or sent into (box == SENT) at least one message;
     * box == null means unfiltered. */
    List<ConversationSummaryView> listAll(EntityId userId, String search, ConversationBox box) {
        return listAll(userId, search, box, null);
    }

    /** Same, plus the lu/non-lu filter (readState == null means unfiltered). */
    List<ConversationSummaryView> listAll(EntityId userId, String search, ConversationBox box,
                                           ConversationReadState readState) {
        Set<EntityId> memberPropertyIds = userAccessPort.memberPropertyIds(userId);
        List<Conversation> groups = conversationRepository.findAllGroupByParticipant(userId);
        List<Conversation> broadcasts =
                conversationRepository.findAllByPropertyIdsAndType(memberPropertyIds, ConversationType.BROADCAST);
        // Unlike BROADCAST (any member reads it), a BOARD_PRIVATE thread is only
        // ever surfaced to properties the caller currently has a staff role on -
        // a plain owner must never see it exists, even in a list they can't open.
        Set<EntityId> staffPropertyIds = memberPropertyIds.stream()
                .filter(propertyId -> userAccessPort.managesProperty(userId, propertyId))
                .collect(Collectors.toSet());
        List<Conversation> boardPrivates =
                conversationRepository.findAllByPropertyIdsAndType(staffPropertyIds, ConversationType.BOARD_PRIVATE);

        List<Conversation> all = new ArrayList<>(groups);
        all.addAll(broadcasts);
        all.addAll(boardPrivates);

        Map<EntityId, String> propertyNameCache = new HashMap<>();
        Map<EntityId, Map<EntityId, String>> memberNamesByPropertyCache = new HashMap<>();
        // Les lots de chaque membre, pour que la recherche accepte un numéro de
        // lot : « A12 » doit ramener la conversation ouverte avec le
        // propriétaire de ce lot, pas seulement celle dont l'objet le cite.
        Map<EntityId, Map<EntityId, List<String>>> memberUnitsByPropertyCache = new HashMap<>();

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
            ConversationSummaryView view = new ConversationSummaryView(conversation.getId(), conversation.getType(), propertyId,
                    propertyName, subject, conversation.getConcernsUnit(), participants, preview, lastMessageAt, unreadCount,
                    messageCount);
            if (!matchesSearch(view, search)) {
                Map<EntityId, List<String>> unitsByUserId =
                        memberUnitsByPropertyCache.computeIfAbsent(propertyId, memberDisplayNameResolver::unitNumbersByUserId);
                if (!matchesUnitNumber(conversation, userId, unitsByUserId, search)) {
                    continue;
                }
            }
            views.add(view);
        }

        return views.stream()
                .filter(view -> matchesReadState(view, readState))
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
        // Le lot que le fil déclare concerner (« Concerne » à la rédaction) -
        // gratuit, il est déjà sur la vue.
        boolean matchesConcernsUnit = view.concernsUnit() != null && view.concernsUnit().toLowerCase(Locale.ROOT).contains(pattern);
        return matchesSubject || matchesAnyParticipant || matchesPropertyName || matchesConcernsUnit;
    }

    /**
     * Le numéro de lot d'un participant. Séparé de matchesSearch parce qu'il
     * demande le registre des lots de la copropriété : on ne le résout que pour
     * les conversations que le reste de la recherche n'a pas déjà retenues, et
     * jamais du tout quand la recherche est vide.
     *
     * <p>Sur les participants, pas sur l'appelant : chercher son propre lot
     * ramènerait toute sa boîte.
     */
    private static boolean matchesUnitNumber(Conversation conversation, EntityId userId,
                                              Map<EntityId, List<String>> unitsByUserId, String search) {
        if (search == null || search.isBlank()) {
            return false;
        }
        String pattern = search.trim().toLowerCase(Locale.ROOT);
        return conversation.getParticipantUserIds().stream()
                .filter(participantId -> !participantId.equals(userId))
                .flatMap(participantId -> unitsByUserId.getOrDefault(participantId, List.of()).stream())
                .anyMatch(unitNumber -> unitNumber.toLowerCase(Locale.ROOT).contains(pattern));
    }

    private static boolean matchesReadState(ConversationSummaryView view, ConversationReadState readState) {
        if (readState == null) {
            return true;
        }
        return readState == ConversationReadState.UNREAD ? view.unreadCount() > 0 : view.unreadCount() == 0;
    }
}
