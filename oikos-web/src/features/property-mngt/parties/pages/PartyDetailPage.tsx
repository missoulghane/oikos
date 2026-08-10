import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useParty } from '@/features/property-mngt/parties/hooks/useParty';
import { usePartyLots } from '@/features/property-mngt/parties/hooks/usePartyLots';
import { useInviteParty } from '@/features/property-mngt/parties/hooks/useInviteParty';
import { EditPartyPhoneForm } from '@/features/property-mngt/parties/components/EditPartyPhoneForm';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { useCurrentUser, canManageProperties } from '@/features/identity/me';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function PartyDetailPage() {
  const { propertyId, partyId: partyIdParam } = useParams<{ propertyId: string; partyId: string }>();
  const partyId = partyIdParam ?? '';
  const party = useParty(partyId);
  const lots = usePartyLots(partyId);
  const invite = useInviteParty();
  const currentUser = useCurrentUser();
  const canInvite = currentUser.data ? canManageProperties(currentUser.data) : false;
  const [isEditingPhone, setIsEditingPhone] = useState(false);

  if (party.isLoading) {
    return <Loader label="Chargement du contact…" />;
  }

  if (party.isError) {
    return <Alert message={getErrorMessage(party.error)} />;
  }

  if (!party.data) {
    return null;
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to={canInvite ? `/property-mngt/properties/${propertyId}/property/contacts` : '/property-ownership/units'}
          className="text-sm text-gray-500 dark:text-gray-400 hover:underline"
        >
          {canInvite ? '← Retour aux contacts' : '← Retour à mes lots'}
        </Link>
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">{party.data.fullName}</h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              {PARTY_TYPE_LABELS[party.data.partyType]} · {party.data.email}
              {party.data.phone && ` · ${party.data.phone}`}
            </p>
            {isEditingPhone ? (
              <div className="mt-2 max-w-xs">
                <EditPartyPhoneForm
                  partyId={partyId}
                  currentPhone={party.data.phone}
                  onSuccess={() => setIsEditingPhone(false)}
                  onCancel={() => setIsEditingPhone(false)}
                />
              </div>
            ) : (
              <Button
                type="button"
                variant="secondary"
                className="mt-2 min-h-0 px-2 py-1 text-xs"
                onClick={() => setIsEditingPhone(true)}
              >
                Modifier le téléphone
              </Button>
            )}
          </div>
          {canInvite && (
            <Button variant="secondary" isLoading={invite.isPending} onClick={() => invite.mutate(partyId)}>
              Inviter à créer un compte
            </Button>
          )}
        </div>
        {invite.isSuccess && (
          <Alert
            variant={invite.data.invited ? 'success' : 'error'}
            message={
              invite.data.invited
                ? 'Invitation envoyée par email.'
                : 'Ce contact a déjà un compte lié : aucune invitation envoyée.'
            }
          />
        )}
        {invite.isError && <Alert message={getErrorMessage(invite.error)} />}
      </div>

      <Card className="flex flex-col gap-2">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Lots associés</h2>

        {lots.isLoading && <Loader label="Chargement des lots…" />}
        {lots.isError && <Alert message={getErrorMessage(lots.error)} />}
        {lots.data && lots.data.length === 0 && (
          <EmptyState title="Aucun lot associé">Ce contact n'est rattaché à aucun lot pour le moment.</EmptyState>
        )}
        {lots.data && lots.data.length > 0 && (
          <ul className="flex flex-col divide-y divide-gray-100 dark:divide-gray-800">
            {lots.data.map((lot) => (
              <li key={lot.id} className="flex items-center justify-between py-2 text-sm">
                <Link
                  to={
                    canInvite
                      ? `/property-mngt/properties/${lot.propertyId}/units/${lot.unitId}`
                      : `/property-ownership/units/${lot.propertyId}/${lot.unitId}`
                  }
                  className="text-gray-700 dark:text-gray-300 hover:underline"
                >
                  {lot.propertyName} — {lot.buildingName} — Lot {lot.unitNumber}
                </Link>
                <span className="text-gray-500 dark:text-gray-400">{lot.ownershipShare}%</span>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
}
