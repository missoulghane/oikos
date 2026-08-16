import type { AttendanceSummary } from '@/features/property-mngt/general-meetings/types/convocation.types';
import { formatWeight } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import { Badge } from '@/shared/components/Badge/Badge';
import { Card } from '@/shared/components/Card/Card';

function Stat({ label, value, hint }: { label: string; value: string; hint?: string }) {
  return (
    <div className="flex flex-col gap-0.5">
      <span className="text-sm text-gray-500 dark:text-gray-400">{label}</span>
      <span className="text-lg font-semibold text-gray-900 dark:text-white/90">{value}</span>
      {hint && <span className="text-xs text-gray-400 dark:text-gray-500">{hint}</span>}
    </div>
  );
}

/**
 * The head of the tracking screen, and what the syndic looks at before opening
 * the session. "Aucun quorum requis" and "quorum non configuré" read the same
 * to a machine but not to a person, which is why quorumRequired is shown
 * rather than inferred from a zero.
 */
export function AttendanceSummaryCard({ summary }: { summary: AttendanceSummary }) {
  return (
    <Card className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h3 className="font-medium text-gray-900 dark:text-white/90">Présences</h3>
        {summary.quorumRequired ? (
          <Badge color={summary.quorumReached ? 'success' : 'error'}>
            Quorum {summary.quorumPercentage.toLocaleString('fr-FR')} %{' '}
            {summary.quorumReached ? 'atteint' : 'non atteint'}
          </Badge>
        ) : (
          <Badge color="light">Aucun quorum requis</Badge>
        )}
      </div>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <Stat label="Lots convoqués" value={String(summary.totalUnits)} />
        <Stat label="Convocations envoyées" value={String(summary.sentCount)} />
        <Stat
          label="Réponses"
          value={`${summary.attendingCount} / ${summary.notAttendingCount}`}
          hint={`présents / absents · ${summary.noReplyCount} sans réponse`}
        />
        <Stat
          label="Émargés"
          value={String(summary.checkedInCount)}
          hint={`${formatWeight(summary.presentWeight)} voix sur ${formatWeight(summary.totalWeight)}`}
        />
      </div>
    </Card>
  );
}
