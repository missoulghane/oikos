import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Badge } from '@/shared/components/Badge/Badge';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { colors } from '@/shared/theme/colors';
import type { ConversationSummary } from '@/features/messaging/types/messaging.types';

export const BROADCAST_CONVERSATION_LABEL = 'Annonces de la copropriété';

// Outlook-style title: the message's subject (see ConversationSubject on
// the backend), not the participant list - "who it's with" is secondary
// info, shown separately (see participantsLine below).
export function conversationTitle(conversation: ConversationSummary): string {
  if (conversation.type === 'BROADCAST') {
    return BROADCAST_CONVERSATION_LABEL;
  }
  return conversation.subject ?? 'Conversation';
}

function participantsLine(conversation: ConversationSummary): string | null {
  if (conversation.type === 'BROADCAST' || conversation.participants.length === 0) {
    return null;
  }
  return conversation.participants.map((participant) => participant.fullName).join(', ');
}

export function ConversationListItem({ conversation, onPress }: { conversation: ConversationSummary; onPress: () => void }) {
  const title = conversationTitle(conversation);
  const participants = participantsLine(conversation);
  const hasUnread = conversation.unreadCount > 0;

  return (
    <Pressable onPress={onPress} style={styles.row}>
      <View style={styles.headerLine}>
        <View style={styles.titleGroup}>
          <Text style={hasUnread ? styles.titleUnread : styles.titleRead} numberOfLines={1}>
            {title}
          </Text>
          {/* A conversation is a message with replies - only shown once
              that's actually true, never for a plain single message. */}
          {conversation.messageCount > 1 && <Text style={styles.messageCount}>({conversation.messageCount} messages)</Text>}
        </View>
        <View style={styles.trailing}>
          {conversation.lastMessageAt && <Text style={styles.time}>{formatRelativeTime(conversation.lastMessageAt)}</Text>}
          {hasUnread && (
            <Badge color="primary" variant="solid">
              {conversation.unreadCount}
            </Badge>
          )}
        </View>
      </View>
      {(conversation.type === 'BOARD_PRIVATE' || conversation.concernsUnit) && (
        <View style={styles.chipsRow}>
          {conversation.type === 'BOARD_PRIVATE' && (
            <View style={styles.warningChip}>
              <Text style={styles.warningChipText}>Privé · bureau</Text>
            </View>
          )}
          {conversation.concernsUnit && (
            <View style={styles.neutralChip}>
              <Text style={styles.neutralChipText}>Concerne {conversation.concernsUnit}</Text>
            </View>
          )}
        </View>
      )}
      <Text style={styles.subtitle} numberOfLines={1}>
        {participants ? `${participants} · ${conversation.propertyName}` : conversation.propertyName}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: {
    gap: 2,
    paddingVertical: 10,
  },
  headerLine: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 8,
  },
  titleGroup: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'baseline',
    gap: 6,
  },
  titleRead: {
    flexShrink: 1,
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[700],
  },
  titleUnread: {
    flexShrink: 1,
    fontSize: 14,
    fontWeight: '700',
    color: colors.gray[900],
  },
  messageCount: {
    fontSize: 12,
    color: colors.gray[400],
  },
  trailing: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  time: {
    fontSize: 12,
    color: colors.gray[400],
  },
  chipsRow: {
    flexDirection: 'row',
    gap: 6,
  },
  warningChip: {
    borderRadius: 999,
    backgroundColor: colors.warning[50],
    paddingHorizontal: 8,
    paddingVertical: 2,
  },
  warningChipText: {
    fontSize: 11,
    fontWeight: '500',
    color: colors.warning[500],
  },
  neutralChip: {
    borderRadius: 999,
    backgroundColor: colors.gray[100],
    paddingHorizontal: 8,
    paddingVertical: 2,
  },
  neutralChipText: {
    fontSize: 11,
    fontWeight: '500',
    color: colors.gray[500],
  },
  subtitle: {
    fontSize: 12,
    color: colors.gray[400],
  },
});
