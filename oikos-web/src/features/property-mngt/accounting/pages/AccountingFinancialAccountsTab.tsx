import { Link, useOutletContext } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useFinancialAccounts } from '@/features/property-mngt/accounting/hooks/useFinancialAccounts';
import { FinancialAccountRow } from '@/features/property-mngt/accounting/components/FinancialAccountRow';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingFinancialAccountsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const financialAccounts = useFinancialAccounts(property.id);

  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;

  return (
    <Card className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-base font-semibold text-gray-900">Comptes financiers</h2>
        {canWrite && (
          <div className="flex flex-wrap gap-3">
            {financialAccounts.data && financialAccounts.data.length > 0 && (
              <Link
                to={`/property-mngt/properties/${property.id}/accounting/financial-accounts/deposit`}
                className="inline-flex min-h-11 items-center rounded-lg bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-theme-xs ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
              >
                Dépôt exceptionnel
              </Link>
            )}
            {financialAccounts.data && financialAccounts.data.length >= 2 && (
              <Link
                to={`/property-mngt/properties/${property.id}/accounting/financial-accounts/transfer`}
                className="inline-flex min-h-11 items-center rounded-lg bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-theme-xs ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
              >
                Virement entre comptes
              </Link>
            )}
            <Link
              to={`/property-mngt/properties/${property.id}/accounting/financial-accounts/new`}
              className="inline-flex min-h-11 items-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
            >
              Créer le compte
            </Link>
          </div>
        )}
      </div>

      {financialAccounts.isLoading && <Loader label="Chargement des comptes financiers…" />}
      {financialAccounts.isError && <Alert message={getErrorMessage(financialAccounts.error)} />}
      {financialAccounts.data && financialAccounts.data.length === 0 && (
        <EmptyState title="Aucun compte financier pour le moment">
          Créez un compte (caisse ou banque) pour commencer à suivre la trésorerie.
        </EmptyState>
      )}
      {financialAccounts.data && financialAccounts.data.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
          {financialAccounts.data.map((account) => (
            <FinancialAccountRow key={account.id} account={account} />
          ))}
        </ul>
      )}
    </Card>
  );
}
