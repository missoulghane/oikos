import { useState } from 'react';
import { useUnits } from '@/features/properties/hooks/useUnits';
import { UNIT_TYPE_LABELS } from '@/features/properties/constants/unitTypeLabels';
import { Loader } from '@/shared/components/Loader/Loader';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Alert } from '@/shared/components/Alert/Alert';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const OWNERSHIP_STATUS_LABELS = {
  SOLD: 'Vendu',
  UNSOLD_DEVELOPER: 'Non vendu (promoteur)',
} as const;

export function UnitList({ buildingId }: { buildingId: string }) {
  const [page, setPage] = useState(0);
  const { data, isLoading, isError, error } = useUnits(buildingId, page);

  if (isLoading) {
    return <Loader label="Chargement des lots…" />;
  }

  if (isError) {
    return <Alert message={getErrorMessage(error)} />;
  }

  if (!data || data.content.length === 0) {
    return <EmptyState title="Aucun lot pour le moment">Ajoutez le premier lot de cet immeuble.</EmptyState>;
  }

  return (
    <div className="flex flex-col gap-3">
      <ul className="flex flex-col divide-y divide-slate-200 rounded-md border border-slate-200">
        {data.content.map((unit) => (
          <li key={unit.id} className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p className="text-sm font-medium text-slate-900">
                Lot {unit.unitNumber} — {UNIT_TYPE_LABELS[unit.unitType]}
              </p>
              <p className="text-sm text-slate-500">{unit.shares} tantièmes</p>
            </div>
            <span className="w-fit rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-600">
              {OWNERSHIP_STATUS_LABELS[unit.ownershipStatus]}
            </span>
          </li>
        ))}
      </ul>
      <Pagination pageNumber={data.pageNumber} totalPages={data.totalPages} onPageChange={setPage} />
    </div>
  );
}
