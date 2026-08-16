import { useAgendaItemResult } from '@/features/property-mngt/general-meetings/hooks/useAgendaItemResult';
import {
  VOTE_CHOICE_LABELS,
  VOTE_OUTCOME_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { formatWeight } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import { Badge } from '@/shared/components/Badge/Badge';

/**
 * The outcome of one resolution, as a copropriétaire reads it: the three
 * counts and the verdict. No nominal vote sheet here - who voted what is the
 * chair's record, not a public listing.
 */
export function AgendaItemResultSummary({ agendaItemId }: { agendaItemId: string }) {
  const result = useAgendaItemResult(agendaItemId);

  if (!result.data) {
    return null;
  }

  return (
    <div className="mt-3 flex flex-wrap items-center gap-3 rounded-lg bg-gray-50 dark:bg-white/[0.03] p-3 text-sm">
      <Badge color={result.data.outcome === 'ADOPTED' ? 'success' : 'error'}>
        {VOTE_OUTCOME_LABELS[result.data.outcome]}
      </Badge>
      <span className="text-gray-600 dark:text-gray-300">
        {VOTE_CHOICE_LABELS.FOR} {formatWeight(result.data.forWeight)} · {VOTE_CHOICE_LABELS.AGAINST}{' '}
        {formatWeight(result.data.againstWeight)} · {VOTE_CHOICE_LABELS.ABSTENTION}{' '}
        {formatWeight(result.data.abstentionWeight)} voix
      </span>
    </div>
  );
}
