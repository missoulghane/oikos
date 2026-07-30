import { useOutletContext } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useFinancialAccounts } from '@/features/property-mngt/accounting/hooks/useFinancialAccounts';
import { CreateFinancialAccountForm } from '@/features/property-mngt/accounting/components/CreateFinancialAccountForm';
import { FinancialAccountRow } from '@/features/property-mngt/accounting/components/FinancialAccountRow';
import { TransferForm } from '@/features/property-mngt/accounting/components/TransferForm';
import { RecordExceptionalDepositForm } from '@/features/property-mngt/accounting/components/RecordExceptionalDepositForm';
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
    <div className="flex flex-col gap-6">
      {canWrite && <CreateFinancialAccountForm propertyId={property.id} />}

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

      {canWrite && financialAccounts.data && financialAccounts.data.length >= 2 && (
        <div className="flex flex-col gap-4">
          <h2 className="text-base font-semibold text-gray-900">Virement entre comptes</h2>
          <TransferForm propertyId={property.id} accounts={financialAccounts.data} />
        </div>
      )}

      {canWrite && financialAccounts.data && financialAccounts.data.length > 0 && (
        <div className="flex flex-col gap-4">
          <h2 className="text-base font-semibold text-gray-900">Dépôt exceptionnel</h2>
          <RecordExceptionalDepositForm propertyId={property.id} accounts={financialAccounts.data} />
        </div>
      )}
    </div>
  );
}
