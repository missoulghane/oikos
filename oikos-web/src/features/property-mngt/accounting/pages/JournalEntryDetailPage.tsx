import { Link, useOutletContext, useParams } from 'react-router-dom';
import { useJournalEntry } from '@/features/property-mngt/accounting/hooks/useJournalEntry';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { JournalEntryLinesTable } from '@/features/property-mngt/accounting/components/JournalEntryLinesTable';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import {
  JOURNAL_CODE_LABELS,
  JOURNAL_ENTRY_STATUS_BADGE_COLORS,
  JOURNAL_ENTRY_STATUS_LABELS,
} from '@/features/property-mngt/accounting/constants/accountingLabels';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function JournalEntryDetailPage() {
  const { property } = useOutletContext<{ property: Property }>();
  const { entryId } = useParams<{ entryId: string }>();
  const entry = useJournalEntry(property.id, entryId ?? '');
  const ledgerAccounts = useLedgerAccounts(property.id);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to={`/property-mngt/properties/${property.id}/accounting/journal`}
          className="text-sm text-gray-500 dark:text-gray-400 hover:underline"
        >
          ← Retour au journal
        </Link>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Écriture comptable</h1>
      </div>

      {entry.isLoading && <Loader label="Chargement de l'écriture…" />}
      {entry.isError && <Alert message={getErrorMessage(entry.error)} />}

      {entry.data && (
        <Card className="flex flex-col gap-4">
          <div className="flex flex-wrap items-center gap-3">
            <p className="text-base font-medium text-gray-900 dark:text-white/90">{JOURNAL_CODE_LABELS[entry.data.journalCode]}</p>
            {entry.data.pieceNumber !== null && (
              <span className="text-sm text-gray-500 dark:text-gray-400">Pièce n°{entry.data.pieceNumber}</span>
            )}
            <Badge color={JOURNAL_ENTRY_STATUS_BADGE_COLORS[entry.data.status]}>
              {JOURNAL_ENTRY_STATUS_LABELS[entry.data.status]}
            </Badge>
          </div>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            {new Date(entry.data.pieceDate).toLocaleDateString('fr-FR')}
            {entry.data.externalReference && ` · ${entry.data.externalReference}`}
          </p>
          <JournalEntryLinesTable lines={entry.data.lines} ledgerAccounts={ledgerAccounts.data} />
        </Card>
      )}
    </div>
  );
}
