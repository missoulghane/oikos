import { useOutletContext } from 'react-router-dom';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { UnitLedgerAccountRow } from '@/features/property-mngt/accounting/components/UnitLedgerAccountRow';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingUnitAccountsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const ledgerAccounts = useLedgerAccounts(property.id);
  const unitAccounts = (ledgerAccounts.data ?? [])
    .filter((account) => account.role === 'UNIT_RECEIVABLE')
    .sort((a, b) => a.accountNumber.localeCompare(b.accountNumber));

  return (
    <Card className="flex flex-col gap-4">
      <div>
        <h2 className="text-base font-semibold text-gray-900">Comptes des lots</h2>
        <p className="text-sm text-gray-500">
          Compte comptable dédié et solde de chaque lot de cette copropriété.
        </p>
      </div>

      {ledgerAccounts.isLoading && <Loader label="Chargement des comptes des lots…" />}
      {ledgerAccounts.isError && <Alert message={getErrorMessage(ledgerAccounts.error)} />}
      {ledgerAccounts.data && unitAccounts.length === 0 && <EmptyState title="Aucun lot pour le moment" />}
      {ledgerAccounts.data && unitAccounts.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
          {unitAccounts.map((account) => (
            <UnitLedgerAccountRow key={account.id} account={account} propertyId={property.id} />
          ))}
        </ul>
      )}
    </Card>
  );
}
