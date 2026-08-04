import { useState } from 'react';
import { Link, useOutletContext } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useFinancialAccounts } from '@/features/property-mngt/accounting/hooks/useFinancialAccounts';
import { useExpenses } from '@/features/property-mngt/accounting/hooks/useExpenses';
import { ExpenseRow } from '@/features/property-mngt/accounting/components/ExpenseRow';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingExpensesTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const [page, setPage] = useState(0);

  const financialAccounts = useFinancialAccounts(property.id);
  const expenses = useExpenses(property.id, page);

  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;
  const accountNameById = new Map((financialAccounts.data ?? []).map((account) => [account.id, account.name]));

  return (
    <Card className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h2 className="text-base font-semibold text-gray-900">Dépenses</h2>
        {canWrite && (
          <Link
            to={`/property-mngt/properties/${property.id}/accounting/expenses/new`}
            className="inline-flex min-h-11 items-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
          >
            Nouvelle dépense
          </Link>
        )}
      </div>

      {expenses.isLoading && <Loader label="Chargement des dépenses…" />}
      {expenses.isError && <Alert message={getErrorMessage(expenses.error)} />}
      {expenses.data && expenses.data.content.length === 0 && (
        <EmptyState title="Aucune dépense enregistrée pour le moment">
          Enregistrez une dépense pour commencer à suivre les sorties de trésorerie.
        </EmptyState>
      )}
      {expenses.data && expenses.data.content.length > 0 && (
        <div className="flex flex-col gap-3">
          <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
            {expenses.data.content.map((expense) => (
              <ExpenseRow
                key={expense.id}
                expense={expense}
                accountName={accountNameById.get(expense.financialAccountId) ?? 'Compte inconnu'}
              />
            ))}
          </ul>
          <Pagination pageNumber={expenses.data.pageNumber} totalPages={expenses.data.totalPages} onPageChange={setPage} />
        </div>
      )}
    </Card>
  );
}
