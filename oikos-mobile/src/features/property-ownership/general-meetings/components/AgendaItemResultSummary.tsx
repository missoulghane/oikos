import { StyleSheet, Text, View } from 'react-native';
import { Badge } from '@/shared/components/Badge/Badge';
import { colors } from '@/shared/theme/colors';
import { useAgendaItemResult } from '@/features/property-ownership/general-meetings/hooks/useAgendaItemResult';
import { VOTE_OUTCOME_LABELS } from '@/features/property-ownership/general-meetings/constants/generalMeetingLabels';
import { formatWeight } from '@/features/property-ownership/general-meetings/utils/formatMeeting';

/**
 * The outcome of one resolution as a copropriétaire reads it: the three counts
 * and the verdict. Never the nominal vote sheet - who voted what is the
 * chair's record, not a public listing.
 */
export function AgendaItemResultSummary({ agendaItemId, enabled }: { agendaItemId: string; enabled: boolean }) {
  const result = useAgendaItemResult(agendaItemId, enabled);

  if (!result.data) {
    return null;
  }

  return (
    <View style={styles.container}>
      <Badge color={result.data.outcome === 'ADOPTED' ? 'success' : 'error'}>
        {VOTE_OUTCOME_LABELS[result.data.outcome]}
      </Badge>
      <Text style={styles.counts}>
        Pour {formatWeight(result.data.forWeight)} · Contre {formatWeight(result.data.againstWeight)} ·
        Abstention {formatWeight(result.data.abstentionWeight)} voix
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginTop: 10,
    gap: 6,
    borderRadius: 10,
    backgroundColor: colors.gray[50],
    padding: 10,
  },
  counts: { fontSize: 12, color: colors.gray[600] },
});
