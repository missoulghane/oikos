import { Link, useOutletContext } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useExpenses } from '@/features/property-mngt/accounting/hooks/useExpenses';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { ExpenseRow } from '@/features/property-mngt/accounting/components/ExpenseRow';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingExpensesTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const expenses = useExpenses(property.id);
  const ledgerAccounts = useLedgerAccounts(property.id);
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;

  return (
    <Card className="flex flex-col gap-4">
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-base font-semibold text-gray-900">Dépenses fournisseurs</h2>
          <p className="text-sm text-gray-500">Règlements versés aux fournisseurs de la copropriété.</p>
        </div>
        {canWrite && (
          <Link
            to={`/property-mngt/properties/${property.id}/accounting/supplier-payments/new`}
            className="inline-flex min-h-11 items-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
          >
            Nouvelle dépense
          </Link>
        )}
      </div>

      {expenses.isLoading && <Loader label="Chargement des dépenses…" />}
      {expenses.isError && <Alert message={getErrorMessage(expenses.error)} />}
      {expenses.data && expenses.data.length === 0 && <EmptyState title="Aucune dépense pour le moment" />}
      {expenses.data && expenses.data.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
          {expenses.data.map((expense) => (
            <ExpenseRow
              key={expense.id}
              expense={expense}
              propertyId={property.id}
              ledgerAccount={ledgerAccounts.data?.find((account) => account.id === expense.ledgerAccountId)}
            />
          ))}
        </ul>
      )}
    </Card>
  );
}
