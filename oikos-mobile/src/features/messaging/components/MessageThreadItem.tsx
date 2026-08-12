import { StyleSheet, Text, View } from 'react-native';
import { colors } from '@/shared/theme/colors';
import type { Message } from '@/features/messaging/types/messaging.types';

// Avatar-led card per message (initials circle) - sender + timestamp header
// line, body paragraph below. No delete/archive/forward, a thread is just a
// scroll of these, oldest first.
export function MessageThreadItem({ message }: { message: Message }) {
  const senderLabel = message.mine ? 'Vous' : message.senderName;

  return (
    <View style={styles.card}>
      <View style={styles.avatar}>
        <Text style={styles.avatarText}>{senderLabel.charAt(0).toUpperCase()}</Text>
      </View>
      <View style={styles.body}>
        <View style={styles.headerLine}>
          <View style={styles.senderGroup}>
            <Text style={styles.senderName}>{senderLabel}</Text>
            {/* OWNER is the unmarked default; only BOARD is called out. */}
            {message.senderIdentity === 'BOARD' && (
              <View style={styles.boardChip}>
                <Text style={styles.boardChipText}>Bureau</Text>
              </View>
            )}
          </View>
          <Text style={styles.time}>
            {new Date(message.createdAt).toLocaleString('fr-FR', {
              day: '2-digit',
              month: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
            })}
          </Text>
        </View>
        <Text style={styles.messageBody}>{message.body}</Text>
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
    justifyContent: 'space-between',
    gap: 8,
  },
  senderGroup: {
    flexDirection: 'row',
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
  messageBody: {
    fontSize: 14,
    color: colors.gray[700],
  },
});
