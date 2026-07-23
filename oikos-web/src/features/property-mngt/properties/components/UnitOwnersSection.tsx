import { useState } from 'react';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { useUnitOwners } from '@/features/property-mngt/properties/hooks/useUnitOwners';
import { AddUnitOwnerForm } from '@/features/property-mngt/properties/components/AddUnitOwnerForm';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function UnitOwnersSection({ unitId }: { unitId: string }) {
  const [isAdding, setIsAdding] = useState(false);
  const { data, isLoading, isError, error } = useUnitOwners(unitId);

  return (
    <div className="flex flex-col gap-2 border-t border-slate-100 pt-2">
      <div className="flex items-center justify-between">
        <p className="text-xs font-medium text-slate-500">Propriétaires</p>
        {!isAdding && (
          <Button type="button" variant="secondary" onClick={() => setIsAdding(true)}>
            Ajouter un propriétaire
          </Button>
        )}
      </div>

      {isAdding && (
        <AddUnitOwnerForm unitId={unitId} onSuccess={() => setIsAdding(false)} onCancel={() => setIsAdding(false)} />
      )}

      {isLoading && <Loader label="Chargement des propriétaires…" />}
      {isError && <Alert message={getErrorMessage(error)} />}
      {data && data.length === 0 && !isAdding && (
        <p className="text-sm text-slate-400">Aucun propriétaire pour le moment.</p>
      )}
      {data && data.length > 0 && (
        <ul className="flex flex-col divide-y divide-slate-100">
          {data.map((owner) => (
            <li key={owner.id} className="flex items-center justify-between py-1 text-sm">
              <span className="text-slate-700">
                {owner.partyFullName} ({PARTY_TYPE_LABELS[owner.partyType]}) — {owner.partyEmail}
              </span>
              <span className="text-slate-500">{owner.ownershipShare}%</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
