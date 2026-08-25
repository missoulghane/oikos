import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useParty } from '@/features/property-mngt/parties/hooks/useParty';
import { usePartyLots } from '@/features/property-mngt/parties/hooks/usePartyLots';
import { InviteContactForLotModal } from '@/features/property-mngt/invitations';
import { EditPartyPhoneForm } from '@/features/property-mngt/parties/components/EditPartyPhoneForm';
import { EditPartyForm } from '@/features/property-mngt/parties/components/EditPartyForm';
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
  const currentUser = useCurrentUser();
  const canInvite = currentUser.data ? canManageProperties(currentUser.data) : false;
  const [isEditingPhone, setIsEditingPhone] = useState(false);
  const [isEditingSheet, setIsEditingSheet] = useState(false);
  const [isInviting, setIsInviting] = useState(false);

  if (party.isLoading) {
    return <Loader label="Chargement du contact…" />;
  }

  if (party.isError) {
    return <Alert message={getErrorMessage(party.error)} />;
  }

  if (!party.data) {
    return null;
  }

  // Ce que la fiche autorise, selon le rapport du contact aux comptes (voir
  // PartyAccountStatus) : rien à modifier quand un compte est rattaché - son
  // titulaire tient son identité à jour et la fiche n'en est que la copie -,
  // tout le reste du temps la fiche entière, avec un avertissement tant qu'une
  // invitation court encore sur l'ancien nom et l'ancienne adresse.
  const accountStatus = party.data.accountStatus;
  const hasAccount = accountStatus === 'ACTIVE';
  const isInvitationPending = accountStatus === 'INVITED';
  const canEditSheet = canInvite && !hasAccount;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to={canInvite ? `/property-mngt/properties/${propertyId}/property/contacts` : '/property-ownership/units'}
          className="text-sm text-gray-500 dark:text-gray-400 hover:underline"
        >
          {canInvite ? '← Retour aux contacts' : '← Retour à mes lots'}
        </Link>
        <div className="flex items-center justify-between gap-3">
          <div className="min-w-0">
            <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">{party.data.fullName}</h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              {PARTY_TYPE_LABELS[party.data.partyType]} · {party.data.email}
              {party.data.phone && ` · ${party.data.phone}`}
            </p>
            {/* Le titulaire du compte garde la seule chose que l'API lui laisse
                changer sur sa propre fiche - son téléphone (voir PATCH
                /parties/{id}/phone). Le syndic, lui, modifie la fiche entière
                depuis la barre d'actions à droite. */}
            {!canInvite &&
              (isEditingPhone ? (
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
              ))}
          </div>
          {/* Les deux actions du syndic sur ce contact, côte à côte. Inviter
              n'a de sens que pour un contact sans compte : l'invitation porte
              sur l'un de ses lots et lui ouvre l'accès à son espace, une fois
              sa demande validée (voir InviteContactForLotModal). */}
          <div className="flex shrink-0 items-center gap-2">
            {canEditSheet && !isEditingSheet && (
              <Button type="button" variant="secondary" onClick={() => setIsEditingSheet(true)}>
                Modifier
              </Button>
            )}
            {canInvite && !hasAccount && (
              <Button variant="secondary" onClick={() => setIsInviting(true)}>
                {isInvitationPending ? "Renvoyer l'invitation" : 'Inviter pour un lot'}
              </Button>
            )}
          </div>
        </div>

        {/* Visible sans ouvrir le formulaire : c'est l'état du contact, pas une
            mise en garde sur la modification. Sans cela le syndic renvoie une
            invitation à quelqu'un qui en a déjà une, ou s'étonne de ne pas voir
            arriver un compte dont le lien dort dans une boîte mail. */}
        {canInvite && isInvitationPending && (
          <p className="mt-2 text-sm text-warning-600 dark:text-warning-500">
            Une invitation est en cours pour ce contact : il ne l'a pas encore acceptée.
          </p>
        )}

        {/* Dit au syndic pourquoi il n'y a rien à modifier ici. Inutile au
            titulaire du compte, qui lit sa propre fiche et sait où la corriger. */}
        {canInvite && hasAccount && (
          <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
            Ce contact est rattaché à un compte : ses informations personnelles sont tenues à jour par son titulaire,
            depuis son profil.
          </p>
        )}

        {/* Dans une carte, comme partout ailleurs : le formulaire se posait à même
            la page, sans fond ni bordure pour le détacher de la fiche qu'il modifie. */}
        {canEditSheet && isEditingSheet && (
          <Card className="mt-3 flex flex-col gap-3">
            {/* L'invitation est rattachée au contact lui-même, pas à une copie
                de sa fiche : corriger son nom ici sera bien repris quand il
                acceptera. Seul l'email déjà parti est figé - il est allé à
                l'ancienne adresse, et rien ne le rattrape. */}
            {isInvitationPending && (
              <Alert
                variant="warning"
                message="Une invitation est en cours pour ce contact. Le lien est déjà parti à son adresse actuelle : si vous la corrigez ici, renvoyez l'invitation après enregistrement pour qu'il la reçoive."
              />
            )}
            <EditPartyForm
              party={party.data}
              onSuccess={() => setIsEditingSheet(false)}
              onCancel={() => setIsEditingSheet(false)}
            />
          </Card>
        )}
      </div>

      {/* Le seul point d'envoi d'une invitation nominative : c'est ici qu'on
          sait de qui l'on parle et pour quel lot. Les lots sont filtrés sur la
          copropriété d'où l'on vient - un contact peut posséder des lots
          ailleurs, et l'invitation vaut pour une copropriété à la fois. */}
      {canInvite && propertyId && (
        <InviteContactForLotModal
          isOpen={isInviting}
          propertyId={propertyId}
          partyId={partyId}
          contactFullName={party.data.fullName}
          contactEmail={party.data.email}
          lots={(lots.data ?? []).filter((lot) => lot.propertyId === propertyId)}
          hasOutstandingInvitation={isInvitationPending}
          onClose={() => setIsInviting(false)}
        />
      )}

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
                  {lot.propertyName} — {lot.buildingName} — {lot.unitNumber}
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
