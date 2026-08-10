import { useOutletContext } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { LedgerAccountRow } from '@/features/property-mngt/accounting/components/LedgerAccountRow';
import { AddBankAccountForm } from '@/features/property-mngt/accounting/components/AddBankAccountForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingTreasuryAccountsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const ledgerAccounts = useLedgerAccounts(property.id);
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;

  const treasuryAccounts = (ledgerAccounts.data ?? []).filter(
    (account) => account.propertyId === property.id && (account.role === 'CASH' || account.role === 'BANK'),
  );

  return (
    <div className="flex flex-col gap-6">
      <Card className="flex flex-col gap-4">
        <div>
          <h2 className="text-base font-semibold text-gray-900">Mes comptes</h2>
          <p className="text-sm text-gray-500">
            Compte de caisse (créé automatiquement) et compte(s) bancaire(s) de cette copropriété.
          </p>
        </div>

        {ledgerAccounts.isLoading && <Loader label="Chargement des comptes…" />}
        {ledgerAccounts.isError && <Alert message={getErrorMessage(ledgerAccounts.error)} />}
        {ledgerAccounts.data && treasuryAccounts.length === 0 && <EmptyState title="Aucun compte pour le moment" />}
        {ledgerAccounts.data && treasuryAccounts.length > 0 && (
          <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
            {treasuryAccounts.map((account) => (
              <LedgerAccountRow key={account.id} account={account} />
            ))}
          </ul>
        )}
      </Card>

      {canWrite && (
        <Card className="flex flex-col gap-4">
          <div>
            <h2 className="text-base font-semibold text-gray-900">Ajouter un compte bancaire</h2>
            <p className="text-sm text-gray-500">
              Ajoutez autant de comptes bancaires que nécessaire pour cette copropriété.
            </p>
          </div>
          <AddBankAccountForm propertyId={property.id} />
        </Card>
      )}
    </div>
  );
}
