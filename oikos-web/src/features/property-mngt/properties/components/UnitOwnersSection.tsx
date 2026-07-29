import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { useUnitOwners } from '@/features/property-mngt/properties/hooks/useUnitOwners';
import { AddUnitOwnerForm } from '@/features/property-mngt/properties/components/AddUnitOwnerForm';
import { AddExistingUnitOwnerForm } from '@/features/property-mngt/properties/components/AddExistingUnitOwnerForm';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

type AddMode = 'none' | 'existing' | 'new';

export function UnitOwnersSection({ unitId, propertyId }: { unitId: string; propertyId: string }) {
  const [addMode, setAddMode] = useState<AddMode>('none');
  const { data, isLoading, isError, error } = useUnitOwners(unitId);

  function close() {
    setAddMode('none');
  }

  return (
    <div className="flex flex-col gap-2 border-t border-gray-100 pt-2">
      <div className="flex items-center justify-between">
        <p className="text-xs font-medium text-gray-500">Propriétaires</p>
        {addMode === 'none' && (
          <div className="flex gap-2">
            <Button type="button" variant="secondary" onClick={() => setAddMode('existing')}>
              Party existante
            </Button>
            <Button type="button" variant="secondary" onClick={() => setAddMode('new')}>
              Nouvelle party
            </Button>
          </div>
        )}
      </div>

      {addMode === 'existing' && (
        <AddExistingUnitOwnerForm unitId={unitId} propertyId={propertyId} onSuccess={close} onCancel={close} />
      )}
      {addMode === 'new' && <AddUnitOwnerForm unitId={unitId} onSuccess={close} onCancel={close} />}

      {isLoading && <Loader label="Chargement des propriétaires…" />}
      {isError && <Alert message={getErrorMessage(error)} />}
      {data && data.length === 0 && addMode === 'none' && (
        <p className="text-sm text-gray-400">Aucun propriétaire pour le moment.</p>
      )}
      {data && data.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-100">
          {data.map((owner) => (
            <li key={owner.id} className="flex items-center justify-between py-1 text-sm">
              <span className="text-gray-700">
                <Link to={`/properties/${propertyId}/parties/${owner.partyId}`} className="hover:underline">
                  {owner.partyFullName}
                </Link>{' '}
                ({PARTY_TYPE_LABELS[owner.partyType]}) — {owner.partyEmail}
              </span>
              <span className="text-gray-500">{owner.ownershipShare}%</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
