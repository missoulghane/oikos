import { Link } from 'react-router-dom';
import { Badge } from '@/shared/components/Badge/Badge';
import {
  JOURNAL_CODE_LABELS,
  JOURNAL_ENTRY_STATUS_BADGE_COLORS,
  JOURNAL_ENTRY_STATUS_LABELS,
} from '@/features/property-mngt/accounting/constants/accountingLabels';
import type { JournalEntry } from '@/features/property-mngt/accounting/types/accounting.types';

export function JournalEntryRow({ entry, propertyId }: { entry: JournalEntry; propertyId: string }) {
  const total = entry.lines
    .filter((line) => line.direction === 'DEBIT')
    .reduce((sum, line) => sum + line.amount, 0);

  return (
    <li className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p className="text-sm font-medium text-gray-900 dark:text-white/90">
          {JOURNAL_CODE_LABELS[entry.journalCode]}
          {entry.pieceNumber !== null && ` — pièce n°${entry.pieceNumber}`}
        </p>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          {new Date(entry.pieceDate).toLocaleDateString('fr-FR')} · {total.toLocaleString('fr-FR')} MAD
          {entry.externalReference && ` · ${entry.externalReference}`}
        </p>
      </div>
      <div className="flex shrink-0 items-center gap-3">
        <Badge color={JOURNAL_ENTRY_STATUS_BADGE_COLORS[entry.status]}>
          {JOURNAL_ENTRY_STATUS_LABELS[entry.status]}
        </Badge>
        <Link
          to={`/property-mngt/properties/${propertyId}/accounting/journal/${entry.id}`}
          className="text-sm font-medium text-brand-500 dark:text-brand-400 hover:underline"
        >
          Voir
        </Link>
      </div>
    </li>
  );
}
