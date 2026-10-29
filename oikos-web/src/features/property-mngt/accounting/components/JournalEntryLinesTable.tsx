import { SortableColumnHeader } from '@/shared/components/SortableColumnHeader/SortableColumnHeader';
import { useTableSort } from '@/shared/hooks/useTableSort';
import { ENTRY_DIRECTION_LABELS } from '@/features/property-mngt/accounting/constants/accountingLabels';
import type { JournalEntryLine, LedgerAccount } from '@/features/property-mngt/accounting/types/accounting.types';

type LineSortField = 'ACCOUNT' | 'AUXILIARY' | 'LABEL' | 'DIRECTION' | 'AMOUNT';

interface JournalEntryLinesTableProps {
  lines: JournalEntryLine[];
  ledgerAccounts?: LedgerAccount[];
}

export function JournalEntryLinesTable({ lines, ledgerAccounts = [] }: JournalEntryLinesTableProps) {
  const totalDebit = lines.filter((line) => line.direction === 'DEBIT').reduce((sum, line) => sum + line.amount, 0);
  const totalCredit = lines.filter((line) => line.direction === 'CREDIT').reduce((sum, line) => sum + line.amount, 0);

  function accountLabel(ledgerAccountId: string): string {
    const account = ledgerAccounts.find((candidate) => candidate.id === ledgerAccountId);
    return account ? `${account.accountNumber} — ${account.label}` : ledgerAccountId;
  }

  // Line order by default: the order the entry was written in, debits first.
  const { sorted, sortBy, sortDirection, onSort } = useTableSort<JournalEntryLine, LineSortField | 'NONE'>(
    lines,
    (line, field) => {
      switch (field) {
        case 'ACCOUNT':
          return accountLabel(line.ledgerAccountId);
        case 'AUXILIARY':
          return line.auxiliaryUnitId ?? line.auxiliaryPartyId;
        case 'LABEL':
          return line.label;
        case 'DIRECTION':
          return ENTRY_DIRECTION_LABELS[line.direction];
        case 'AMOUNT':
          return line.amount;
        default:
          return null;
      }
    },
    { field: 'NONE', direction: 'ASC' },
  );

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-800 text-sm">
        <thead>
          <tr className="text-left text-gray-500 dark:text-gray-400">
            <SortableColumnHeader
              field="ACCOUNT"
              activeField={sortBy}
              direction={sortDirection}
              onSort={onSort}
              className="pr-4"
            >
              Compte
            </SortableColumnHeader>
            <SortableColumnHeader
              field="AUXILIARY"
              activeField={sortBy}
              direction={sortDirection}
              onSort={onSort}
              className="pr-4"
            >
              Auxiliaire
            </SortableColumnHeader>
            <SortableColumnHeader
              field="LABEL"
              activeField={sortBy}
              direction={sortDirection}
              onSort={onSort}
              className="pr-4"
            >
              Libellé
            </SortableColumnHeader>
            <SortableColumnHeader
              field="DIRECTION"
              activeField={sortBy}
              direction={sortDirection}
              onSort={onSort}
              className="pr-4"
            >
              Sens
            </SortableColumnHeader>
            <SortableColumnHeader
              field="AMOUNT"
              activeField={sortBy}
              direction={sortDirection}
              onSort={onSort}
              align="right"
              className="pr-4"
            >
              Montant
            </SortableColumnHeader>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
          {sorted.map((line) => (
            <tr key={line.id} className="text-gray-800 dark:text-white/90">
              <td className="py-2 pr-4">{accountLabel(line.ledgerAccountId)}</td>
              <td className="py-2 pr-4 font-mono text-xs text-gray-500 dark:text-gray-400">
                {line.auxiliaryUnitId ?? line.auxiliaryPartyId ?? '—'}
              </td>
              <td className="py-2 pr-4">{line.label}</td>
              <td className="py-2 pr-4">{ENTRY_DIRECTION_LABELS[line.direction]}</td>
              <td className="py-2 pr-4 text-right">{line.amount.toLocaleString('fr-FR')} MAD</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr className="border-t border-gray-200 dark:border-gray-800 font-medium text-gray-900 dark:text-white/90">
            <td className="py-2 pr-4" colSpan={4}>
              Total
            </td>
            <td className="py-2 pr-4 text-right">
              {totalDebit.toLocaleString('fr-FR')} / {totalCredit.toLocaleString('fr-FR')} MAD
            </td>
          </tr>
        </tfoot>
      </table>
    </div>
  );
}
