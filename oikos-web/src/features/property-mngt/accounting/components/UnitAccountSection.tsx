import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { useUnitAccount } from '@/features/property-mngt/accounting/hooks/useUnitAccount';
import { useUnitAccountMovements } from '@/features/property-mngt/accounting/hooks/useUnitAccountMovements';
import { UnitAccountMovementRow } from '@/features/property-mngt/accounting/components/UnitAccountMovementRow';
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
        <div className="flex flex-wrap gap-3 border-t border-gray-200 pt-4">
          <Link
            to={`/properties/${propertyId}/units/${unitId}/payment`}
            className="inline-flex min-h-11 items-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
          >
            Enregistrer un paiement
          </Link>
          <Link
            to={`/properties/${propertyId}/units/${unitId}/regularization`}
            className="inline-flex min-h-11 items-center rounded-lg bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-theme-xs ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
          >
            Enregistrer une régularisation
          </Link>
        </div>
      )}
    </div>
  );
}
