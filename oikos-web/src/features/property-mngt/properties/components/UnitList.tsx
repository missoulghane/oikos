import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useUnits } from '@/features/property-mngt/properties/hooks/useUnits';
import { Loader } from '@/shared/components/Loader/Loader';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Alert } from '@/shared/components/Alert/Alert';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { OwnershipStatus } from '@/features/property-mngt/properties/types/property.types';

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
  const isFiltered = search !== '' || ownershipStatus !== '';
  const { data, isLoading, isError, error } = useUnits(
    buildingId,
    page,
    search || undefined,
    ownershipStatus || undefined,
  );

  if (isLoading) {
    return <Loader label="Chargement des lots…" />;
  }

  if (isError) {
    return <Alert message={getErrorMessage(error)} />;
  }

  return (
    <div className="flex flex-col gap-3">
      <div className="grid gap-3 sm:grid-cols-2">
        <Input
          label="Rechercher un lot (numéro d'appartement, propriétaire : nom, téléphone)"
          // Input/Select derive the label's htmlFor from id ?? name: without
          // one, the label stays detached from the field. Scoped by building,
          // since one list is rendered per building on the same page.
          name={`unit-search-${buildingId}`}
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(0);
          }}
        />
        <Select
          label="Affectation"
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
      {data && data.content.length === 0 && (
        <EmptyState title={isFiltered ? 'Aucun lot trouvé' : 'Aucun lot pour le moment'}>
          {isFiltered ? 'Ajustez votre recherche ou vos filtres.' : "Ajoutez le premier lot de cet immeuble."}
        </EmptyState>
      )}
      {data && data.content.length > 0 && (
        <>
          <ul className="flex flex-col divide-y divide-gray-200 dark:divide-gray-800 rounded-lg border border-gray-200 dark:border-gray-800">
            {data.content.map((unit) => {
              const secondaryLine = [showShares ? `${unit.shares} tantièmes` : null, unit.ownerFullNames.join(', ') || null]
                .filter(Boolean)
                .join(' · ');

              return (
                <li key={unit.id}>
                  <Link
                    to={`/property-mngt/properties/${propertyId}/units/${unit.id}`}
                    className="flex items-center justify-between gap-2 px-3 py-2 hover:bg-gray-50 dark:hover:bg-white/[0.03]"
                  >
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-gray-900 dark:text-white/90">
                        Lot {unit.unitNumber} — {unit.unitTypeName}
                      </p>
                      {secondaryLine && <p className="truncate text-sm text-gray-500 dark:text-gray-400">{secondaryLine}</p>}
                    </div>
                    <span className="w-fit shrink-0 rounded-full bg-gray-100 dark:bg-white/[0.05] px-2 py-1 text-xs font-medium text-gray-600 dark:text-gray-400">
                      {OWNERSHIP_STATUS_LABELS[unit.ownershipStatus]}
                    </span>
                  </Link>
                </li>
              );
            })}
          </ul>
          <Pagination pageNumber={data.pageNumber} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  );
}
