import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { usePropertyInstallments } from '@/features/property-mngt/installments/hooks/usePropertyInstallments';
import {
  InstallmentFilters,
  type InstallmentFiltersValue,
} from '@/features/property-mngt/installments/components/InstallmentFilters';
import { InstallmentList } from '@/features/property-mngt/installments/components/InstallmentList';
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
  sortBy: 'DUE_DATE',
  sortDirection: 'ASC',
};

export function InstallmentsListTab() {
  const { property } = useOutletContext<{ property: Property }>();

  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState<InstallmentFiltersValue>(DEFAULT_FILTERS);

  function handleFiltersChange(next: InstallmentFiltersValue) {
    setFilters(next);
    setPage(0);
  }

  const queryFilters: InstallmentListFilters = {
    status: filters.status ? [filters.status] : undefined,
    dueDateFrom: filters.dueDateFrom || undefined,
    dueDateTo: filters.dueDateTo || undefined,
    sortBy: filters.sortBy,
    sortDirection: filters.sortDirection,
  };

  const installments = usePropertyInstallments(property.id, page, queryFilters);

  return (
    <div className="flex flex-col gap-4">
      <InstallmentFilters value={filters} onChange={handleFiltersChange} />

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
    </div>
  );
}
