import { Link, useOutletContext } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { useExpenses } from '@/features/property-mngt/accounting/hooks/useExpenses';
import { useLatestPayment } from '@/features/property-mngt/installments';
import { TreasuryAccountCard } from '@/features/property-mngt/accounting/components/TreasuryAccountCard';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingOverviewTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const ledgerAccounts = useLedgerAccounts(property.id);
  const expenses = useExpenses(property.id);
  const latestPayment = useLatestPayment(property.id);
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;

  const treasuryAccounts = (ledgerAccounts.data ?? []).filter(
    (account) => account.role === 'CASH' || account.role === 'BANK',
  );
  const lastExpense = (expenses.data ?? [])
    .slice()
    .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())[0];
  const lastExpenseLedgerAccount = lastExpense
    ? ledgerAccounts.data?.find((account) => account.id === lastExpense.ledgerAccountId)
    : undefined;

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
          <h2 className="text-sm font-semibold text-gray-900">Dernière dépense</h2>
          {expenses.isLoading && <Loader label="Chargement…" />}
          {expenses.isError && <Alert message={getErrorMessage(expenses.error)} />}
          {expenses.data && !lastExpense && <p className="text-sm text-gray-500">Aucune dépense pour le moment.</p>}
          {lastExpense && (
            <>
              <p className="text-lg font-semibold text-gray-900">
                {lastExpense.amount.toLocaleString('fr-FR')} MAD
              </p>
              <p className="text-sm text-gray-500">
                {new Date(lastExpense.date).toLocaleDateString('fr-FR')}
                {lastExpenseLedgerAccount && ` · ${lastExpenseLedgerAccount.label}`}
                {lastExpense.description && ` · ${lastExpense.description}`}
              </p>
              <Link
                to={`/property-mngt/properties/${property.id}/accounting/expenses`}
                className="text-sm font-medium text-brand-500 hover:underline"
              >
                Voir les dépenses
              </Link>
            </>
          )}
        </Card>

        <Card className="flex flex-col gap-2">
          <h2 className="text-sm font-semibold text-gray-900">Dernière recette</h2>
          {latestPayment.isLoading && <Loader label="Chargement…" />}
          {latestPayment.isError && <Alert message={getErrorMessage(latestPayment.error)} />}
          {latestPayment.data === null && <p className="text-sm text-gray-500">Aucune recette pour le moment.</p>}
          {latestPayment.data && (
            <>
              <p className="text-lg font-semibold text-gray-900">
                {latestPayment.data.amount.toLocaleString('fr-FR')} MAD
              </p>
              <p className="text-sm text-gray-500">
                {new Date(latestPayment.data.valueDate).toLocaleDateString('fr-FR')} ·{' '}
                {PAYMENT_MODE_LABELS[latestPayment.data.mode]}
              </p>
              <Link
                to={`/property-mngt/properties/${property.id}/accounting/journal/${latestPayment.data.journalEntryId}`}
                className="text-sm font-medium text-brand-500 hover:underline"
              >
                Voir l'écriture
              </Link>
            </>
          )}
        </Card>
      </div>
    </div>
  );
}
