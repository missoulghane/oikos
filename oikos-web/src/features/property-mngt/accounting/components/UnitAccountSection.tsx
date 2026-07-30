import { useState } from 'react';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useUnitAccount } from '@/features/property-mngt/accounting/hooks/useUnitAccount';
import { useUnitAccountMovements } from '@/features/property-mngt/accounting/hooks/useUnitAccountMovements';
import { useFinancialAccounts } from '@/features/property-mngt/accounting/hooks/useFinancialAccounts';
import { UnitAccountMovementRow } from '@/features/property-mngt/accounting/components/UnitAccountMovementRow';
import { RecordPaymentForm } from '@/features/property-mngt/accounting/components/RecordPaymentForm';
import { RegularizationForm } from '@/features/property-mngt/accounting/components/RegularizationForm';
import { LettrageProposalCard } from '@/features/property-mngt/accounting/components/LettrageProposalCard';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

function balanceColorClass(balance: number): string {
  if (balance < 0) return 'text-error-600';
  if (balance > 0) return 'text-success-600';
  return 'text-gray-500';
}

interface UnitAccountSectionProps {
  propertyId: string;
  unitId: string;
}

export function UnitAccountSection({ propertyId, unitId }: UnitAccountSectionProps) {
  const [page, setPage] = useState(0);
  const currentUser = useCurrentUser();
  const account = useUnitAccount(unitId);
  const movements = useUnitAccountMovements(unitId, page);
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, propertyId) : false;
  const financialAccounts = useFinancialAccounts(propertyId, canWrite);

  return (
    <div className="flex flex-col gap-4">
      {account.isLoading && <Loader label="Chargement du solde…" />}
      {account.isError && <Alert message={getErrorMessage(account.error)} />}
      {account.data && (
        <p className="text-lg font-semibold">
          Solde : <span className={balanceColorClass(account.data.balance)}>{account.data.balance.toLocaleString('fr-FR')} MAD</span>
        </p>
      )}

      {movements.isLoading && <Loader label="Chargement des mouvements…" />}
      {movements.isError && <Alert message={getErrorMessage(movements.error)} />}
      {movements.data && movements.data.content.length === 0 && (
        <EmptyState title="Aucun mouvement pour le moment" />
      )}
      {movements.data && movements.data.content.length > 0 && (
        <div className="flex flex-col gap-3">
          <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
            {movements.data.content.map((movement) => (
              <UnitAccountMovementRow key={movement.id} movement={movement} />
            ))}
          </ul>
          <Pagination pageNumber={movements.data.pageNumber} totalPages={movements.data.totalPages} onPageChange={setPage} />
        </div>
      )}

      {canWrite && <LettrageProposalCard propertyId={propertyId} unitId={unitId} />}

      {canWrite && (
        <div className="flex flex-col gap-4 border-t border-gray-200 pt-4">
          <RecordPaymentForm propertyId={propertyId} unitId={unitId} accounts={financialAccounts.data ?? []} />
          <RegularizationForm propertyId={propertyId} unitId={unitId} />
        </div>
      )}
    </div>
  );
}
