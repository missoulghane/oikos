import type { Expense } from '@/features/property-mngt/accounting/types/accounting.types';

interface ExpenseRowProps {
  expense: Expense;
  accountName: string;
}

export function ExpenseRow({ expense, accountName }: ExpenseRowProps) {
  return (
    <li className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p className="text-sm font-medium text-gray-900">
          {expense.category} — {expense.provider}
        </p>
        <p className="text-sm text-gray-500">
          {new Date(expense.date).toLocaleDateString('fr-FR')} · {accountName}
          {expense.description ? ` · ${expense.description}` : ''}
        </p>
      </div>
      <p className="text-sm font-medium text-gray-900">{expense.amount.toLocaleString('fr-FR')} MAD</p>
    </li>
  );
}
