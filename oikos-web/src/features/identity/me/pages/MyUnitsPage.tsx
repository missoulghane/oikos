import { Link } from 'react-router-dom';
import { useMyUnits } from '@/features/identity/me/hooks/useMyUnits';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function MyUnitsPage() {
  const units = useMyUnits();

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900">Mes lots</h1>

      <Card className="flex flex-col gap-2">
        {units.isLoading && <Loader label="Chargement de vos lots…" />}
        {units.isError && <Alert message={getErrorMessage(units.error)} />}
        {units.data && units.data.length === 0 && (
          <EmptyState title="Aucun lot">Vous n'êtes propriétaire d'aucun lot pour le moment.</EmptyState>
        )}
        {units.data && units.data.length > 0 && (
          <ul className="flex flex-col divide-y divide-gray-100">
            {units.data.map((unit) => (
              <li key={unit.unitId} className="flex items-center justify-between py-2 text-sm">
                <Link to={`/properties/${unit.propertyId}/units/${unit.unitId}`} className="text-gray-700 hover:underline">
                  {unit.propertyName} — {unit.buildingName} — Lot {unit.unitNumber}
                </Link>
                <span className="text-gray-500">{unit.ownershipShare}%</span>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
}
