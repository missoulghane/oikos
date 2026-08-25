import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useUnits } from '@/features/property-mngt/properties/hooks/useUnits';
import { Loader } from '@/shared/components/Loader/Loader';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Alert } from '@/shared/components/Alert/Alert';
import { Select } from '@/shared/components/Select/Select';
import { FilterPanel } from '@/shared/components/FilterPanel/FilterPanel';
import { SortableColumnHeader } from '@/shared/components/SortableColumnHeader/SortableColumnHeader';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { floorLabel } from '@/features/property-mngt/properties/utils/floorLabel';
import { countActiveFilters } from '@/shared/utils/countActiveFilters';
import { nextSortDirection, type SortDirection } from '@/shared/utils/sorting';
import type { OwnershipStatus, UnitSortField } from '@/features/property-mngt/properties/types/property.types';

const OWNERSHIP_STATUS_LABELS = {
  AFFECTED: 'Affecté',
  NOT_AFFECTED: 'Non affecté',
} as const;

// '' is the "no filter" choice - a <select> value is always a string, so the
// empty option carries the absence of filter rather than a separate flag.
type OwnershipStatusFilter = OwnershipStatus | '';

interface UnitListProps {
  buildingId: string;
  propertyId: string;
  showShares: boolean;
}

export function UnitList({ buildingId, propertyId, showShares }: UnitListProps) {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [ownershipStatus, setOwnershipStatus] = useState<OwnershipStatusFilter>('');
  // Lot number ascending: the order a syndic reads a building in.
  const [sortBy, setSortBy] = useState<UnitSortField>('UNIT_NUMBER');
  const [sortDirection, setSortDirection] = useState<SortDirection>('ASC');
  const isFiltered = search !== '' || ownershipStatus !== '';
  const { data, isLoading, isError, error } = useUnits(buildingId, page, {
    search: search || undefined,
    ownershipStatus: ownershipStatus || undefined,
    sortBy,
    sortDirection,
  });

  function handleSort(field: UnitSortField) {
    setSortDirection(nextSortDirection(field, sortBy, sortDirection));
    setSortBy(field);
    setPage(0);
  }

  if (isLoading) {
    return <Loader label="Chargement des lots…" />;
  }

  if (isError) {
    return <Alert message={getErrorMessage(error)} />;
  }

  return (
    <div className="flex flex-col gap-3">
      <FilterPanel
        activeCount={countActiveFilters({ ownershipStatus, search }, { ownershipStatus: '', search: '' })}
        onClear={() => {
          setSearch('');
          setOwnershipStatus('');
          setPage(0);
        }}
        search={{
          value: search,
          onChange: (next) => {
            setSearch(next);
            setPage(0);
          },
          placeholder: 'Rechercher un lot, un propriétaire…',
        }}
      >
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <Select
            label="Affectation"
            // Input/Select derive the label's htmlFor from id ?? name: without
            // one, the label stays detached from the field. Scoped by building,
            // since one list is rendered per building on the same page.
            name={`ownership-status-${buildingId}`}
            value={ownershipStatus}
            onChange={(e) => {
              setOwnershipStatus(e.target.value as OwnershipStatusFilter);
              setPage(0);
            }}
          >
            <option value="">Tous les lots</option>
            <option value="AFFECTED">{OWNERSHIP_STATUS_LABELS.AFFECTED}</option>
            <option value="NOT_AFFECTED">{OWNERSHIP_STATUS_LABELS.NOT_AFFECTED}</option>
          </Select>
        </div>
      </FilterPanel>
      {data && data.content.length === 0 && (
        <EmptyState title={isFiltered ? 'Aucun lot trouvé' : 'Aucun lot pour le moment'}>
          {isFiltered ? 'Ajustez votre recherche ou vos filtres.' : "Ajoutez le premier lot de cet immeuble."}
        </EmptyState>
      )}
      {data && data.content.length > 0 && (
        <>
          <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-gray-800">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-800 text-sm">
              <thead>
                <tr className="text-left text-gray-500 dark:text-gray-400">
                  <SortableColumnHeader
                    field="UNIT_NUMBER"
                    activeField={sortBy}
                    direction={sortDirection}
                    onSort={handleSort}
                    className="px-3"
                  >
                    Lot
                  </SortableColumnHeader>
                  <th className="px-3 py-2 font-medium">Type</th>
                  <th className="px-3 py-2 font-medium">Étage</th>
                  {/* Only sortable when it is displayed - a header for a hidden
                      column would sort on something the reader cannot see. */}
                  {showShares && (
                    <SortableColumnHeader
                      field="SHARES"
                      activeField={sortBy}
                      direction={sortDirection}
                      onSort={handleSort}
                      align="right"
                      className="px-3"
                    >
                      Tantièmes
                    </SortableColumnHeader>
                  )}
                  <th className="px-3 py-2 font-medium">Propriétaires</th>
                  <th className="px-3 py-2 font-medium">Affectation</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                {data.content.map((unit) => (
                  <tr key={unit.id} className="hover:bg-gray-50 dark:hover:bg-white/[0.03]">
                    {/* A real link rather than a click handler on the row: it keeps
                        the keyboard, the middle click and "open in a new tab". */}
                    <td className="px-3 py-2">
                      <Link
                        to={`/property-mngt/properties/${propertyId}/units/${unit.id}`}
                        className="font-medium text-gray-900 hover:underline dark:text-white/90"
                      >
                        {unit.unitNumber} — {unit.unitTypeName}
                      </Link>
                    </td>
                    <td className="px-3 py-2 text-gray-500 dark:text-gray-400">{unit.unitTypeName}</td>
                    {/* Un tiret plutôt qu'une case vide : l'étage n'est pas renseigné
                        sur les lots générés en masse, ce n'est pas une donnée manquante. */}
                    <td className="px-3 py-2 text-gray-500 dark:text-gray-400">
                      {typeof unit.floor === 'number' ? floorLabel(unit.floor) : '—'}
                    </td>
                    {showShares && (
                      <td className="px-3 py-2 text-right text-gray-700 dark:text-gray-300">{unit.shares}</td>
                    )}
                    <td className="px-3 py-2 text-gray-500 dark:text-gray-400">
                      {unit.ownerFullNames.join(', ') || '—'}
                    </td>
                    <td className="px-3 py-2">
                      <span className="w-fit rounded-full bg-gray-100 px-2 py-1 text-xs font-medium text-gray-600 dark:bg-white/[0.05] dark:text-gray-400">
                        {OWNERSHIP_STATUS_LABELS[unit.ownershipStatus]}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination pageNumber={data.pageNumber} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  );
}
