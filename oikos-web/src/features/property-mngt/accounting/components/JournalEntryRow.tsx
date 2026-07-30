import { Badge } from '@/shared/components/Badge/Badge';
import {
  FINANCIAL_ENTRY_DIRECTION_BADGE_COLORS,
  FINANCIAL_ENTRY_DIRECTION_LABELS,
  FINANCIAL_ENTRY_TYPE_LABELS,
} from '@/features/property-mngt/accounting/constants/financialEntryLabels';
import type { FinancialJournalEntry } from '@/features/property-mngt/accounting/types/accounting.types';

interface JournalEntryRowProps {
  entry: FinancialJournalEntry;
  accountName: string;
}

export function JournalEntryRow({ entry, accountName }: JournalEntryRowProps) {
  return (
    <li className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div className="flex items-center gap-2">
          <p className="text-sm font-medium text-gray-900">{FINANCIAL_ENTRY_TYPE_LABELS[entry.type]}</p>
          <Badge color={FINANCIAL_ENTRY_DIRECTION_BADGE_COLORS[entry.direction]}>
            {FINANCIAL_ENTRY_DIRECTION_LABELS[entry.direction]}
          </Badge>
        </div>
        <p className="text-sm text-gray-500">
          {new Date(entry.date).toLocaleDateString('fr-FR')} · {accountName} · {entry.label}
        </p>
      </div>
      <p className="text-sm font-medium text-gray-900">{entry.amount.toLocaleString('fr-FR')} MAD</p>
    </li>
  );
}
