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
import { FilterPanel } from '@/shared/components/FilterPanel/FilterPanel';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { countActiveFilters } from '@/shared/utils/countActiveFilters';
import { nextSortDirection } from '@/shared/utils/sorting';
import type { InstallmentListFilters } from '@/features/property-mngt/installments/types/installment.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

// Sorting always holds a value, so it would inflate the "active filters" count
// on an untouched list - it is still reset by "Effacer les filtres".
const SORT_KEYS = ['sortBy', 'sortDirection'] as const;

const DEFAULT_FILTERS: InstallmentFiltersValue = {
  status: '',
  search: '',
  includeNotYetDue: false,
  dueDateFrom: '',
  dueDateTo: '',
  installmentCallId: '',
  // Most recent first, like the owner space: the echeances a syndic acts on are
  // the latest ones, and an ascending list opens on the oldest history instead.
  sortBy: 'DUE_DATE',
  sortDirection: 'DESC',
};

export function InstallmentsListTab() {
  const [searchParams] = useSearchParams();
  const installmentCallIdFromUrl = searchParams.get('installmentCallId') ?? '';
  // The syndic dashboard's "à collecter" badge links here with ?status=NOT_SETTLED, and
  // means the list it counted: unpaid, and already due. "Already due" is this screen's
  // default (includeNotYetDue false), so the badge only has to name the status.
  const statusFromUrl = (searchParams.get('status') ?? '') as InstallmentFiltersValue['status'];

  // Keyed on the url params: "Voir les échéances" on the Appels de fonds tab
  // links here with ?installmentCallId=... - remounting (instead of syncing
  // via an effect) resets the filter state to that value, including when
  // clicking that link again for a different call while already on this tab.
  return (
    <InstallmentsListTabContent
      key={`${installmentCallIdFromUrl}-${statusFromUrl}`}
      installmentCallIdFromUrl={installmentCallIdFromUrl}
      statusFromUrl={statusFromUrl}
    />
  );
}

function InstallmentsListTabContent({
  installmentCallIdFromUrl,
  statusFromUrl,
}: {
  installmentCallIdFromUrl: string;
  statusFromUrl: InstallmentFiltersValue['status'];
}) {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const canWrite = currentUser.data ? canWriteAccounting(currentUser.data, property.id) : false;
  const regularize = useRegularizePropertyInstallments(property.id);

  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState<InstallmentFiltersValue>({
    ...DEFAULT_FILTERS,
    installmentCallId: installmentCallIdFromUrl,
    status: statusFromUrl,
  });

  function handleFiltersChange(next: InstallmentFiltersValue) {
    setFilters(next);
    setPage(0);
  }

  function handleSort(field: InstallmentFiltersValue['sortBy']) {
    handleFiltersChange({
      ...filters,
      sortBy: field,
      sortDirection: nextSortDirection(field, filters.sortBy, filters.sortDirection),
    });
  }

  const queryFilters: InstallmentListFilters = {
    status: filters.status ? [filters.status] : undefined,
    dueDateFrom: filters.dueDateFrom || undefined,
    dueDateTo: filters.dueDateTo || undefined,
    installmentCallId: filters.installmentCallId || undefined,
    search: filters.search || undefined,
    // Sent explicitly: the API defaults to returning everything.
    excludeNotYetDue: filters.includeNotYetDue ? undefined : true,
    sortBy: filters.sortBy,
    sortDirection: filters.sortDirection,
  };

  const installments = usePropertyInstallments(property.id, page, queryFilters);

  return (
    <Card className="flex flex-col gap-4">
      {canWrite && (
        <div className="flex flex-col gap-2 border-b border-gray-100 dark:border-gray-800 pb-4">
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

      <FilterPanel
        activeCount={countActiveFilters(filters, DEFAULT_FILTERS, SORT_KEYS)}
        onClear={() => handleFiltersChange(DEFAULT_FILTERS)}
        search={{
          value: filters.search,
          onChange: (search) => handleFiltersChange({ ...filters, search }),
          placeholder: 'Rechercher un lot, un propriétaire…',
        }}
      >
        <InstallmentFilters propertyId={property.id} value={filters} onChange={handleFiltersChange} />
      </FilterPanel>

      {installments.isLoading && <Loader label="Chargement des échéances…" />}
      {installments.isError && <Alert message={getErrorMessage(installments.error)} />}
      {installments.data && installments.data.content.length === 0 && (
        <EmptyState title="Aucune échéance">Aucune échéance ne correspond à ces critères.</EmptyState>
      )}
      {installments.data && installments.data.content.length > 0 && (
        <div className="flex flex-col gap-3">
          <InstallmentList
            installments={installments.data.content}
            sortBy={filters.sortBy}
            sortDirection={filters.sortDirection}
            onSort={handleSort}
          />
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
