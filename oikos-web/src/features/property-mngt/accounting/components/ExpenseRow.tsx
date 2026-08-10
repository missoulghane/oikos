import { Link } from 'react-router-dom';
import type { Expense, LedgerAccount } from '@/features/property-mngt/accounting/types/accounting.types';

interface ExpenseRowProps {
  expense: Expense;
  propertyId: string;
  ledgerAccount: LedgerAccount | undefined;
}

export function ExpenseRow({ expense, propertyId, ledgerAccount }: ExpenseRowProps) {
  return (
    <li className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p className="text-sm font-medium text-gray-900 dark:text-white/90">
          {ledgerAccount ? `${ledgerAccount.accountNumber} — ${ledgerAccount.label}` : 'Compte de charge'}
        </p>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          {new Date(expense.date).toLocaleDateString('fr-FR')} · {expense.amount.toLocaleString('fr-FR')} MAD
          {expense.description && ` · ${expense.description}`}
        </p>
      </div>
      <Link
        to={`/property-mngt/properties/${propertyId}/accounting/journal/${expense.journalEntryId}`}
        className="shrink-0 text-sm font-medium text-brand-500 dark:text-brand-400 hover:underline"
      >
        Voir l'écriture
      </Link>
    </li>
  );
}
