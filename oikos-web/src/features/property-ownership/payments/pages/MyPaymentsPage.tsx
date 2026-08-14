import { useMemo, useState } from 'react';
import { useMyPayments } from '@/features/property-ownership/payments/hooks/useMyPayments';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { MyPaymentFilters } from '@/features/property-ownership/payments/components/MyPaymentFilters';
import { MyPaymentsTable } from '@/features/property-ownership/payments/components/MyPaymentsTable';
import {
  DEFAULT_MY_PAYMENT_FILTERS,
  filterPayments,
  type MyPaymentFiltersValue,
} from '@/features/property-ownership/payments/utils/filterPayments';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { FilterPanel } from '@/shared/components/FilterPanel/FilterPanel';
import { nextSortDirection } from '@/shared/utils/sorting';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { countActiveFilters } from '@/shared/utils/countActiveFilters';

const PAGE_SIZE = 10;

// Sorting is always set to something, so it would inflate the "active filters"
// count on an untouched list - it is still reset by "Effacer les filtres".
const SORT_KEYS = ['sortBy', 'sortDirection'] as const;

export function MyPaymentsPage() {
  const payments = useMyPayments();
  const units = useMyUnits();
  const [filters, setFilters] = useState<MyPaymentFiltersValue>(DEFAULT_MY_PAYMENT_FILTERS);
  const [page, setPage] = useState(0);

  const isLoading = payments.isLoading || units.isLoading;
  const error = payments.error ?? units.error;

  const unitsById = useMemo(
    () => new Map((units.data ?? []).map((unit) => [unit.unitId, unit])),
    [units.data],
  );
  const unitLabelById = useMemo(
    () => new Map((units.data ?? []).map((unit) => [unit.unitId, formatUnitLabel(unit)])),
    [units.data],
  );

  const filtered = useMemo(
    () => filterPayments(payments.data ?? [], filters, unitLabelById),
    [payments.data, filters, unitLabelById],
  );

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const pageRows = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  function handleFiltersChange(next: MyPaymentFiltersValue) {
    setFilters(next);
    setPage(0);
  }

  function handleSort(field: MyPaymentFiltersValue['sortBy']) {
    handleFiltersChange({
      ...filters,
      sortBy: field,
      sortDirection: nextSortDirection(field, filters.sortBy, filters.sortDirection),
    });
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Mes paiements</h1>

      <Card className="flex flex-col gap-4">
        <FilterPanel
          activeCount={countActiveFilters(filters, DEFAULT_MY_PAYMENT_FILTERS, SORT_KEYS)}
          onClear={() => handleFiltersChange(DEFAULT_MY_PAYMENT_FILTERS)}
          search={{
            value: filters.search,
            onChange: (search) => handleFiltersChange({ ...filters, search }),
            placeholder: 'Rechercher un lot, un montant, une date…',
          }}
        >
          <MyPaymentFilters value={filters} onChange={handleFiltersChange} />
        </FilterPanel>

        {isLoading && <Loader label="Chargement de vos paiements…" />}
        {error && <Alert message={getErrorMessage(error)} />}

        {!isLoading && !error && filtered.length === 0 && (
          <EmptyState title="Aucun paiement">
            {(payments.data ?? []).length === 0
              ? "Vous n'avez aucun paiement pour le moment."
              : 'Aucun paiement ne correspond à ces filtres.'}
          </EmptyState>
        )}

        {filtered.length > 0 && (
          <>
            <MyPaymentsTable
              payments={pageRows}
              unitsById={unitsById}
              sortBy={filters.sortBy}
              sortDirection={filters.sortDirection}
              onSort={handleSort}
            />
            <Pagination pageNumber={page} totalPages={totalPages} onPageChange={setPage} />
          </>
        )}
      </Card>
    </div>
  );
}
