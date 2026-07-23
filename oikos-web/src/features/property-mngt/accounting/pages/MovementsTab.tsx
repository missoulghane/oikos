import { useState } from 'react';
import { isAxiosError } from 'axios';
import { useOutletContext } from 'react-router-dom';
import { useAccountByHolder } from '@/features/property-mngt/accounting/hooks/useAccountByHolder';
import { useAccountMovements } from '@/features/property-mngt/accounting/hooks/useAccountMovements';
import { MovementList } from '@/features/property-mngt/accounting/components/MovementList';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function MovementsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [page, setPage] = useState(0);

  const account = useAccountByHolder(property.id, 'PROPERTY');
  const accountId = account.data?.id ?? '';
  const movements = useAccountMovements(accountId, page, Boolean(accountId));

  if (account.isLoading) {
    return <Loader label="Chargement du compte…" />;
  }

  if (account.isError) {
    if (isAxiosError(account.error) && account.error.response?.status === 404) {
      return (
        <EmptyState title="Aucun compte comptable pour cette copropriété">
          Les mouvements comptables apparaîtront ici dès qu'un compte sera créé.
        </EmptyState>
      );
    }
    return <Alert message={getErrorMessage(account.error)} />;
  }

  return (
    <div className="flex flex-col gap-4">
      <p className="text-sm text-slate-500">
        Solde du compte : <span className="font-medium text-slate-900">{account.data?.balance} MAD</span>
      </p>

      {movements.isLoading && <Loader label="Chargement des mouvements…" />}
      {movements.isError && <Alert message={getErrorMessage(movements.error)} />}
      {movements.data && movements.data.content.length === 0 && (
        <EmptyState title="Aucun mouvement">Aucun mouvement comptable pour le moment.</EmptyState>
      )}
      {movements.data && movements.data.content.length > 0 && (
        <div className="flex flex-col gap-3">
          <MovementList movements={movements.data.content} />
          <Pagination
            pageNumber={movements.data.pageNumber}
            totalPages={movements.data.totalPages}
            onPageChange={setPage}
          />
        </div>
      )}
    </div>
  );
}
