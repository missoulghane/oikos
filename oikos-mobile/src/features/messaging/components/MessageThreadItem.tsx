import { StyleSheet, Text, View } from 'react-native';
import { colors } from '@/shared/theme/colors';
import { renderMessageBody } from '@/features/messaging/utils/renderMessageBody';
import type { Message } from '@/features/messaging/types/messaging.types';

interface MessageThreadItemProps {
  message: Message;
  /** Who this message went to, e.g. the other participant(s)' names or a
   * broadcast audience label - conversation-level (same for every message in
   * the thread), since individual messages don't carry their own recipient
   * list. */
  recipientLabel: string;
}

// Avatar-led card per message (initials circle) - "De : … à …" / "A : …"
// header lines, body paragraph below. No delete/archive/forward, a thread is
// just a scroll of these, oldest first.
export function MessageThreadItem({ message, recipientLabel }: MessageThreadItemProps) {
  const senderLabel = message.mine ? 'Vous' : message.senderName;
  const toLabel = message.mine ? recipientLabel : 'Vous';
  const dateLabel = new Date(message.createdAt).toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <View style={styles.card}>
      <View style={styles.avatar}>
        <Text style={styles.avatarText}>{senderLabel.charAt(0).toUpperCase()}</Text>
      </View>
      <View style={styles.body}>
        <View style={styles.headerLine}>
          <View style={styles.senderGroup}>
            <Text style={styles.senderName}>De : {senderLabel}</Text>
            <Text style={styles.time}>à {dateLabel}</Text>
            {/* OWNER is the unmarked default; only BOARD is called out. */}
            {message.senderIdentity === 'BOARD' && (
              <View style={styles.boardChip}>
                <Text style={styles.boardChipText}>Bureau</Text>
              </View>
            )}
          </View>
        </View>
        <Text style={styles.toLine}>A : {toLabel}</Text>
        <View style={styles.messageBody}>{renderMessageBody(message.body, styles.messageBodyText)}</View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    gap: 12,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.gray[100],
    padding: 16,
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: colors.brand[50],
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.brand[500],
  },
  body: {
    flex: 1,
    gap: 4,
  },
  headerLine: {
    flexDirection: 'row',
    alignItems: 'baseline',
    gap: 8,
  },
  senderGroup: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'baseline',
    gap: 6,
  },
  senderName: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.gray[900],
  },
  boardChip: {
    borderRadius: 999,
    backgroundColor: colors.warning[50],
    paddingHorizontal: 8,
    paddingVertical: 2,
  },
  boardChipText: {
    fontSize: 11,
    fontWeight: '500',
    color: colors.warning[500],
  },
  time: {
    fontSize: 12,
    color: colors.gray[400],
  },
  toLine: {
    fontSize: 12,
    color: colors.gray[500],
  },
  messageBody: {
    marginTop: 4,
  },
  messageBodyText: {
    fontSize: 14,
    color: colors.gray[700],
  },
});
