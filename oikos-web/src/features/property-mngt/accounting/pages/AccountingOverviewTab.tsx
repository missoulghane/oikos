import { Link, useOutletContext } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { useExpenses } from '@/features/property-mngt/accounting/hooks/useExpenses';
import { ExpenseRow } from '@/features/property-mngt/accounting/components/ExpenseRow';
import { useLatestPayments, PaymentRow } from '@/features/property-mngt/installments';
import { TreasuryAccountCard } from '@/features/property-mngt/accounting/components/TreasuryAccountCard';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

// How many recent expenses/payments the overview shows - kept as a single named
// constant precisely so it stays a one-line change, per the product ask.
const RECENT_ITEMS_COUNT = 5;

export function AccountingOverviewTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const ledgerAccounts = useLedgerAccounts(property.id);
  const expenses = useExpenses(property.id);
  const latestPayments = useLatestPayments(property.id, RECENT_ITEMS_COUNT);
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;

  const treasuryAccounts = (ledgerAccounts.data ?? []).filter(
    (account) => account.role === 'CASH' || account.role === 'BANK',
  );
  const recentExpenses = (expenses.data ?? [])
    .slice()
    .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
    .slice(0, RECENT_ITEMS_COUNT);

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-4">
        {canWrite && (
          <div className="flex justify-end">
            <Link
              to={`/property-mngt/properties/${property.id}/accounting/treasury-accounts/new`}
              className="inline-flex min-h-11 items-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
            >
              Ajouter un compte
            </Link>
          </div>
        )}

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {ledgerAccounts.isLoading && <Loader label="Chargement des comptes…" />}
          {ledgerAccounts.isError && <Alert message={getErrorMessage(ledgerAccounts.error)} />}
          {ledgerAccounts.data && treasuryAccounts.length === 0 && (
            <EmptyState title="Aucun compte de caisse ou de banque pour le moment" />
          )}
          {treasuryAccounts.map((account) => (
            <TreasuryAccountCard key={account.id} account={account} propertyId={property.id} />
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Card className="flex flex-col gap-2">
          <div className="flex items-center justify-between gap-2">
            <h2 className="text-sm font-semibold text-gray-900 dark:text-white/90">Dernières dépenses</h2>
            <Link
              to={`/property-mngt/properties/${property.id}/accounting/expenses`}
              className="text-sm font-medium text-brand-500 dark:text-brand-400 hover:underline"
            >
              Voir tout
            </Link>
          </div>
          {expenses.isLoading && <Loader label="Chargement…" />}
          {expenses.isError && <Alert message={getErrorMessage(expenses.error)} />}
          {expenses.data && recentExpenses.length === 0 && (
            <p className="text-sm text-gray-500 dark:text-gray-400">Aucune dépense pour le moment.</p>
          )}
          {recentExpenses.length > 0 && (
            <ul className="flex flex-col divide-y divide-gray-200 dark:divide-gray-800 rounded-lg border border-gray-200 dark:border-gray-800">
              {recentExpenses.map((expense) => (
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

        <Card className="flex flex-col gap-2">
          <div className="flex items-center justify-between gap-2">
            <h2 className="text-sm font-semibold text-gray-900 dark:text-white/90">Dernières recettes</h2>
            <Link
              to={`/property-mngt/properties/${property.id}/accounting/journal`}
              className="text-sm font-medium text-brand-500 dark:text-brand-400 hover:underline"
            >
              Voir tout
            </Link>
          </div>
          {latestPayments.isLoading && <Loader label="Chargement…" />}
          {latestPayments.isError && <Alert message={getErrorMessage(latestPayments.error)} />}
          {latestPayments.data && latestPayments.data.length === 0 && (
            <p className="text-sm text-gray-500 dark:text-gray-400">Aucune recette pour le moment.</p>
          )}
          {latestPayments.data && latestPayments.data.length > 0 && (
            <ul className="flex flex-col divide-y divide-gray-200 dark:divide-gray-800 rounded-lg border border-gray-200 dark:border-gray-800">
              {latestPayments.data.map((payment) => (
                <PaymentRow key={payment.id} payment={payment} propertyId={property.id} />
              ))}
            </ul>
          )}
        </Card>
      </div>
    </div>
  );
}
