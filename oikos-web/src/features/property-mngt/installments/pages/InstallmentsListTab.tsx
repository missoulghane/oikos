import { useState } from 'react';
import { useOutletContext, useSearchParams } from 'react-router-dom';
import { useCurrentUser, canWriteAccounting } from '@/features/identity/me';
import { usePropertyInstallments } from '@/features/property-mngt/installments/hooks/usePropertyInstallments';
import { useRegularizePropertyInstallments } from '@/features/property-mngt/installments/hooks/useRegularizePropertyInstallments';
import {
  InstallmentFilters,
  type InstallmentFiltersValue,
} from '@/features/property-mngt/installments/components/InstallmentFilters';
import { InstallmentList } from '@/features/property-mngt/installments/components/InstallmentList';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { InstallmentListFilters } from '@/features/property-mngt/installments/types/installment.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

const DEFAULT_FILTERS: InstallmentFiltersValue = {
  status: '',
  dueDateFrom: '',
  dueDateTo: '',
  installmentCallId: '',
  sortBy: 'DUE_DATE',
  sortDirection: 'ASC',
};

export function InstallmentsListTab() {
  const [searchParams] = useSearchParams();
  const installmentCallIdFromUrl = searchParams.get('installmentCallId') ?? '';

  // Keyed on the url param: "Voir les échéances" on the Appels de fonds tab
  // links here with ?installmentCallId=... - remounting (instead of syncing
  // via an effect) resets the filter state to that value, including when
  // clicking that link again for a different call while already on this tab.
  return <InstallmentsListTabContent key={installmentCallIdFromUrl} installmentCallIdFromUrl={installmentCallIdFromUrl} />;
}

function InstallmentsListTabContent({ installmentCallIdFromUrl }: { installmentCallIdFromUrl: string }) {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;
  const regularize = useRegularizePropertyInstallments(property.id);

  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState<InstallmentFiltersValue>({
    ...DEFAULT_FILTERS,
    installmentCallId: installmentCallIdFromUrl,
  });

  function handleFiltersChange(next: InstallmentFiltersValue) {
    setFilters(next);
    setPage(0);
  }

  const queryFilters: InstallmentListFilters = {
    status: filters.status ? [filters.status] : undefined,
    dueDateFrom: filters.dueDateFrom || undefined,
    dueDateTo: filters.dueDateTo || undefined,
    installmentCallId: filters.installmentCallId || undefined,
    sortBy: filters.sortBy,
    sortDirection: filters.sortDirection,
  };

  const installments = usePropertyInstallments(property.id, page, queryFilters);

  return (
    <Card className="flex flex-col gap-4">
      {canWrite && (
        <div className="flex flex-col gap-2 border-b border-gray-100 pb-4">
          <Button
            type="button"
            variant="secondary"
            className="self-start"
            isLoading={regularize.isPending}
            onClick={() => regularize.mutate()}
          >
            Régulariser les avances (toute la copropriété)
          </Button>
          {regularize.isError && <Alert message={getErrorMessage(regularize.error)} />}
          {regularize.isSuccess && (
            <Alert
              variant="success"
              message={
                regularize.data.unitsRegularized === 0
                  ? 'Aucune régularisation nécessaire.'
                  : `${regularize.data.totalAmountApplied.toLocaleString('fr-FR')} MAD imputé(s) sur ${regularize.data.unitsRegularized} lot(s).`
              }
            />
          )}
        </div>
      )}

      <InstallmentFilters propertyId={property.id} value={filters} onChange={handleFiltersChange} />

      {installments.isLoading && <Loader label="Chargement des échéances…" />}
      {installments.isError && <Alert message={getErrorMessage(installments.error)} />}
      {installments.data && installments.data.content.length === 0 && (
        <EmptyState title="Aucune échéance">Aucune échéance ne correspond à ces critères.</EmptyState>
      )}
      {installments.data && installments.data.content.length > 0 && (
        <div className="flex flex-col gap-3">
          <InstallmentList installments={installments.data.content} />
          <Pagination
            pageNumber={installments.data.pageNumber}
            totalPages={installments.data.totalPages}
            onPageChange={setPage}
          />
        </div>
      )}
    </Card>
  );
}
