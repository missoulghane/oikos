import { useMemo, useState } from 'react';
import { useMyInstallments } from '@/features/property-ownership/installments/hooks/useMyInstallments';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { MyInstallmentFilters } from '@/features/property-ownership/installments/components/MyInstallmentFilters';
import { MyInstallmentsTable } from '@/features/property-ownership/installments/components/MyInstallmentsTable';
import { outstandingTotal } from '@/features/property-ownership/installments/utils/installmentTotals';
import {
  DEFAULT_MY_INSTALLMENT_FILTERS,
  filterInstallments,
  type MyInstallmentFiltersValue,
} from '@/features/property-ownership/installments/utils/filterInstallments';
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

export function MyInstallmentsPage() {
  const installments = useMyInstallments();
  const units = useMyUnits();
  const [filters, setFilters] = useState<MyInstallmentFiltersValue>(DEFAULT_MY_INSTALLMENT_FILTERS);
  const [page, setPage] = useState(0);

  const isLoading = installments.isLoading || units.isLoading;
  const error = installments.error ?? units.error;

  const unitsById = useMemo(
    () => new Map((units.data ?? []).map((unit) => [unit.unitId, unit])),
    [units.data],
  );
  const unitLabelById = useMemo(
    () => new Map((units.data ?? []).map((unit) => [unit.unitId, formatUnitLabel(unit)])),
    [units.data],
  );

  // Computed over the whole dataset, never over the filtered rows: the badge
  // states what is owed in total, and clicking it is what narrows the table.
  const totalDue = outstandingTotal(installments.data ?? []);

  const filtered = useMemo(
    () => filterInstallments(installments.data ?? [], filters, unitLabelById),
    [installments.data, filters, unitLabelById],
  );

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const pageRows = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  function handleFiltersChange(next: MyInstallmentFiltersValue) {
    setFilters(next);
    setPage(0);
  }

  function handleSort(field: MyInstallmentFiltersValue['sortBy']) {
    handleFiltersChange({
      ...filters,
      sortBy: field,
      sortDirection: nextSortDirection(field, filters.sortBy, filters.sortDirection),
    });
  }

  const isDueFilterActive = filters.status === 'DUE';

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Mes échéances</h1>
        <button
          type="button"
          aria-pressed={isDueFilterActive}
          onClick={() => handleFiltersChange({ ...filters, status: isDueFilterActive ? '' : 'DUE' })}
          className={`flex w-fit items-center gap-2 rounded-full px-4 py-2 text-sm font-medium transition ${
            isDueFilterActive
              ? 'bg-brand-500 text-white'
              : 'bg-brand-50 text-brand-600 hover:bg-brand-100 dark:bg-brand-500/[0.12] dark:text-brand-400'
          }`}
        >
          Total à régler
          <span className="font-semibold">{totalDue.toLocaleString('fr-FR')} MAD</span>
        </button>
      </div>

      <Card className="flex flex-col gap-4">
        <FilterPanel
          activeCount={countActiveFilters(filters, DEFAULT_MY_INSTALLMENT_FILTERS, SORT_KEYS)}
          onClear={() => handleFiltersChange(DEFAULT_MY_INSTALLMENT_FILTERS)}
          search={{
            value: filters.search,
            onChange: (search) => handleFiltersChange({ ...filters, search }),
            placeholder: 'Rechercher un lot, un montant, une date…',
          }}
        >
          <MyInstallmentFilters value={filters} onChange={handleFiltersChange} />
        </FilterPanel>

        {isLoading && <Loader label="Chargement de vos échéances…" />}
        {error && <Alert message={getErrorMessage(error)} />}

        {!isLoading && !error && filtered.length === 0 && (
          <EmptyState title="Aucune échéance">
            {(installments.data ?? []).length === 0
              ? "Vous n'avez aucune échéance pour le moment."
              : 'Aucune échéance ne correspond à ces filtres.'}
          </EmptyState>
        )}

        {filtered.length > 0 && (
          <>
            <MyInstallmentsTable
              installments={pageRows}
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
