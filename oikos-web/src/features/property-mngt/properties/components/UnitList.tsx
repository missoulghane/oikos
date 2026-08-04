import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useUnits } from '@/features/property-mngt/properties/hooks/useUnits';
import { Loader } from '@/shared/components/Loader/Loader';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Alert } from '@/shared/components/Alert/Alert';
import { Input } from '@/shared/components/Input/Input';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const OWNERSHIP_STATUS_LABELS = {
  AFFECTED: 'Affecté',
  NOT_AFFECTED: 'Non affecté',
} as const;

interface UnitListProps {
  buildingId: string;
  propertyId: string;
  showShares: boolean;
}

export function UnitList({ buildingId, propertyId, showShares }: UnitListProps) {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const { data, isLoading, isError, error } = useUnits(buildingId, page, search || undefined);

  if (isLoading) {
    return <Loader label="Chargement des lots…" />;
  }

  if (isError) {
    return <Alert message={getErrorMessage(error)} />;
  }

  return (
    <div className="flex flex-col gap-3">
      <Input
        label="Rechercher un lot (propriétaire : nom, téléphone)"
        value={search}
        onChange={(e) => {
          setSearch(e.target.value);
          setPage(0);
        }}
      />
      {data && data.content.length === 0 && (
        <EmptyState title={search ? 'Aucun lot trouvé' : 'Aucun lot pour le moment'}>
          {search ? 'Ajustez votre recherche.' : "Ajoutez le premier lot de cet immeuble."}
        </EmptyState>
      )}
      {data && data.content.length > 0 && (
        <>
          <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
            {data.content.map((unit) => {
              const secondaryLine = [showShares ? `${unit.shares} tantièmes` : null, unit.ownerFullNames.join(', ') || null]
                .filter(Boolean)
                .join(' · ');

              return (
                <li key={unit.id}>
                  <Link
                    to={`/property-mngt/properties/${propertyId}/units/${unit.id}`}
                    className="flex items-center justify-between gap-2 px-3 py-2 hover:bg-gray-50"
                  >
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-gray-900">
                        Lot {unit.unitNumber} — {unit.unitTypeName}
                      </p>
                      {secondaryLine && <p className="truncate text-sm text-gray-500">{secondaryLine}</p>}
                    </div>
                    <span className="w-fit shrink-0 rounded-full bg-gray-100 px-2 py-1 text-xs font-medium text-gray-600">
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
