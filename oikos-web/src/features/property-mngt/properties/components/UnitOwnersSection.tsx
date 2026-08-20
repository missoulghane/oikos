import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { useUnitOwners } from '@/features/property-mngt/properties/hooks/useUnitOwners';
import { AddUnitOwnerForm } from '@/features/property-mngt/properties/components/AddUnitOwnerForm';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

interface UnitOwnersSectionProps {
  unitId: string;
  propertyId: string;
  /** Hides the attach/create-owner actions for callers without ownership-management rights (e.g. the owner's own read-only lot page). */
  canManage?: boolean;
}

export function UnitOwnersSection({ unitId, propertyId, canManage = true }: UnitOwnersSectionProps) {
  const [isAttaching, setIsAttaching] = useState(false);
  const { data, isLoading, isError, error } = useUnitOwners(unitId);

  function close() {
    setIsAttaching(false);
  }

  return (
    <div className="flex flex-col gap-2 border-t border-gray-100 dark:border-gray-800 pt-2">
      <div className="flex items-center justify-between">
        <p className="text-xs font-medium text-gray-500 dark:text-gray-400">Propriétaires</p>
        {/* Un seul bouton : le formulaire reconnaît lui-même un contact déjà
            enregistré à son email ou à son téléphone, et propose de le
            rattacher. Deux boutons demandaient au syndic de savoir d'avance ce
            que la recherche établit mieux que lui. */}
        {canManage && !isAttaching && (
          <Button type="button" variant="secondary" onClick={() => setIsAttaching(true)}>
            Rattacher à un contact
          </Button>
        )}
      </div>

      {canManage && isAttaching && (
        <AddUnitOwnerForm unitId={unitId} propertyId={propertyId} onSuccess={close} onCancel={close} />
      )}

      {isLoading && <Loader label="Chargement des propriétaires…" />}
      {isError && <Alert message={getErrorMessage(error)} />}
      {data && data.length === 0 && !isAttaching && (
        <p className="text-sm text-gray-400 dark:text-gray-500">Aucun propriétaire pour le moment.</p>
      )}
      {data && data.length > 0 && (
        <ul className="flex flex-col divide-y divide-gray-100 dark:divide-gray-800">
          {data.map((owner) => (
            <li key={owner.id} className="flex items-center justify-between py-1 text-sm">
              <span className="text-gray-700 dark:text-gray-300">
                <Link to={`/parties/${propertyId}/${owner.partyId}`} className="hover:underline">
                  {owner.partyFullName}
                </Link>{' '}
                ({PARTY_TYPE_LABELS[owner.partyType]}) — {owner.partyEmail}
              </span>
              <span className="text-gray-500 dark:text-gray-400">{owner.ownershipShare}%</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
