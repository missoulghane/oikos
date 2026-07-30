import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useFinancialAccounts } from '@/features/property-mngt/accounting/hooks/useFinancialAccounts';
import { useExpenses } from '@/features/property-mngt/accounting/hooks/useExpenses';
import { RecordExpenseForm } from '@/features/property-mngt/accounting/components/RecordExpenseForm';
import { ExpenseRow } from '@/features/property-mngt/accounting/components/ExpenseRow';
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
    <div className="flex flex-col gap-6">
      {canWrite && <RecordExpenseForm propertyId={property.id} accounts={financialAccounts.data ?? []} />}

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
    </div>
  );
}
