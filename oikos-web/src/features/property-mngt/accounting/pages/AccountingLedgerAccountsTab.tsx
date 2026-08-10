import { useOutletContext } from 'react-router-dom';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { LedgerAccountRow } from '@/features/property-mngt/accounting/components/LedgerAccountRow';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingLedgerAccountsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const ledgerAccounts = useLedgerAccounts(property.id);

  return (
    <Card className="flex flex-col gap-4">
      <div>
        <h2 className="text-base font-semibold text-gray-900">Plan comptable</h2>
        <p className="text-sm text-gray-500">
          Comptes partagés du plan comptable marocain et comptes propres à cette copropriété (caisse, banque, lots).
        </p>
      </div>

      {ledgerAccounts.isLoading && <Loader label="Chargement du plan comptable…" />}
      {ledgerAccounts.isError && <Alert message={getErrorMessage(ledgerAccounts.error)} />}
      {ledgerAccounts.data && ledgerAccounts.data.length === 0 && (
        <EmptyState title="Aucun compte pour le moment" />
      )}
      {ledgerAccounts.data && ledgerAccounts.data.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
          {ledgerAccounts.data
            .slice()
            .sort((a, b) => a.accountNumber.localeCompare(b.accountNumber))
            .map((account) => (
              <LedgerAccountRow key={account.id} account={account} />
            ))}
        </ul>
      )}
    </Card>
  );
}
