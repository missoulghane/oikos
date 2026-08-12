import { Pressable, StyleSheet, Text, View } from 'react-native';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { colors } from '@/shared/theme/colors';
import type { MessageDraftSummary } from '@/features/messaging/types/messaging.types';

function draftTitle(draft: MessageDraftSummary): string {
  return draft.subject?.trim() || 'Sans titre';
}

function recipientsLine(draft: MessageDraftSummary): string {
  if (draft.broadcast) {
    return 'Toute la copropriété';
  }
  if (draft.recipients.length === 0) {
    return 'Aucun destinataire';
  }
  return draft.recipients.map((recipient) => recipient.fullName).join(', ');
}

interface DraftListItemProps {
  draft: MessageDraftSummary;
  onOpen: () => void;
  onDelete: () => void;
  isDeleting?: boolean;
}

export function DraftListItem({ draft, onOpen, onDelete, isDeleting = false }: DraftListItemProps) {
  return (
    <View style={styles.row}>
      <Pressable onPress={onOpen} style={styles.content}>
        <View style={styles.headerLine}>
          <Text style={styles.title} numberOfLines={1}>
            {draftTitle(draft)}
          </Text>
          <Text style={styles.time}>Modifié {formatRelativeTime(draft.lastModifiedAt)}</Text>
        </View>
        <Text style={styles.subtitle} numberOfLines={1}>
          {recipientsLine(draft)} · {draft.propertyName}
        </Text>
      </Pressable>
      <Pressable disabled={isDeleting} onPress={onDelete} accessibilityLabel={`Supprimer le brouillon ${draftTitle(draft)}`} style={styles.deleteButton}>
        <Text style={styles.deleteButtonText}>Supprimer</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  content: {
    flex: 1,
    gap: 2,
    paddingVertical: 10,
  },
  headerLine: {
    flexDirection: 'row',
    alignItems: 'baseline',
    justifyContent: 'space-between',
    gap: 8,
  },
  title: {
    flex: 1,
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[700],
  },
  time: {
    fontSize: 12,
    color: colors.gray[400],
  },
  subtitle: {
    fontSize: 12,
    color: colors.gray[400],
  },
  deleteButton: {
    paddingHorizontal: 4,
    paddingVertical: 6,
  },
  deleteButtonText: {
    fontSize: 13,
    fontWeight: '500',
    color: colors.error[500],
  },
});
