import { ENTRY_DIRECTION_LABELS } from '@/features/property-mngt/accounting/constants/accountingLabels';
import type { JournalEntryLine, LedgerAccount } from '@/features/property-mngt/accounting/types/accounting.types';

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

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200 text-sm">
        <thead>
          <tr className="text-left text-gray-500">
            <th className="py-2 pr-4 font-medium">Compte</th>
            <th className="py-2 pr-4 font-medium">Auxiliaire</th>
            <th className="py-2 pr-4 font-medium">Libellé</th>
            <th className="py-2 pr-4 font-medium">Sens</th>
            <th className="py-2 pr-4 text-right font-medium">Montant</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {lines.map((line) => (
            <tr key={line.id} className="text-gray-800">
              <td className="py-2 pr-4">{accountLabel(line.ledgerAccountId)}</td>
              <td className="py-2 pr-4 font-mono text-xs text-gray-500">
                {line.auxiliaryUnitId ?? line.auxiliaryPartyId ?? '—'}
              </td>
              <td className="py-2 pr-4">{line.label}</td>
              <td className="py-2 pr-4">{ENTRY_DIRECTION_LABELS[line.direction]}</td>
              <td className="py-2 pr-4 text-right">{line.amount.toLocaleString('fr-FR')} MAD</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr className="border-t border-gray-200 font-medium text-gray-900">
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
