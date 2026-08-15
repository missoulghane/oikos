import { Link } from 'react-router-dom';
import { Badge } from '@/shared/components/Badge/Badge';
import {
  JOURNAL_CODE_LABELS,
  JOURNAL_ENTRY_STATUS_BADGE_COLORS,
  JOURNAL_ENTRY_STATUS_LABELS,
} from '@/features/property-mngt/accounting/constants/accountingLabels';
import { SortableColumnHeader } from '@/shared/components/SortableColumnHeader/SortableColumnHeader';
import type { SortDirection } from '@/shared/utils/sorting';
import type {
  JournalEntry,
  JournalEntrySortField,
} from '@/features/property-mngt/accounting/types/accounting.types';

interface JournalEntryTableProps {
  entries: JournalEntry[];
  propertyId: string;
  sortBy: JournalEntrySortField;
  sortDirection: SortDirection;
  onSort: (field: JournalEntrySortField) => void;
}

/**
 * The sortable form of JournalEntryRow, for the one listing that filters and
 * sorts. The journal tab keeps the plain row list: it has no criteria bar, so a
 * table there would be columns with nothing to click.
 *
 * Montant carries no header button: it is summed here from the debit lines, so
 * the API - which paginates - cannot order on it, and a sort applied to the
 * rows on screen would only reorder one page.
 */
export function JournalEntryTable({ entries, propertyId, sortBy, sortDirection, onSort }: JournalEntryTableProps) {
  return (
    <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-gray-800">
      <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-800 text-sm">
        <thead>
          <tr className="text-left text-gray-500 dark:text-gray-400">
            <SortableColumnHeader
              field="PIECE_DATE"
              activeField={sortBy}
              direction={sortDirection}
              onSort={onSort}
              className="px-3"
            >
              Date
            </SortableColumnHeader>
            <SortableColumnHeader
              field="PIECE_NUMBER"
              activeField={sortBy}
              direction={sortDirection}
              onSort={onSort}
              className="px-3"
            >
              Pièce
            </SortableColumnHeader>
            <th className="px-3 py-2 font-medium">Référence</th>
            <th className="px-3 py-2 text-right font-medium">Montant</th>
            <th className="px-3 py-2 font-medium">Statut</th>
            <th className="px-3 py-2">
              <span className="sr-only">Détail</span>
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
          {entries.map((entry) => {
            const total = entry.lines
              .filter((line) => line.direction === 'DEBIT')
              .reduce((sum, line) => sum + line.amount, 0);

            return (
              <tr key={entry.id} className="hover:bg-gray-50 dark:hover:bg-white/[0.03]">
                <td className="px-3 py-2 text-gray-900 dark:text-white/90">
                  {new Date(entry.pieceDate).toLocaleDateString('fr-FR')}
                </td>
                <td className="px-3 py-2 text-gray-700 dark:text-gray-300">
                  {JOURNAL_CODE_LABELS[entry.journalCode]}
                  {entry.pieceNumber !== null && ` n°${entry.pieceNumber}`}
                </td>
                <td className="px-3 py-2 text-gray-500 dark:text-gray-400">{entry.externalReference ?? '—'}</td>
                <td className="px-3 py-2 text-right text-gray-700 dark:text-gray-300">
                  {total.toLocaleString('fr-FR')} MAD
                </td>
                <td className="px-3 py-2">
                  <Badge color={JOURNAL_ENTRY_STATUS_BADGE_COLORS[entry.status]}>
                    {JOURNAL_ENTRY_STATUS_LABELS[entry.status]}
                  </Badge>
                </td>
                <td className="px-3 py-2">
                  <Link
                    to={`/property-mngt/properties/${propertyId}/accounting/journal/${entry.id}`}
                    className="text-sm font-medium text-brand-500 hover:underline dark:text-brand-400"
                  >
                    Voir
                  </Link>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
