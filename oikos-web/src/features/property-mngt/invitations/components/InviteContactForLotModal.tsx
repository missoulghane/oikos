import { useState } from 'react';
import { Modal } from '@/shared/components/Modal/Modal';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { CopyLinkButton } from '@/features/property-mngt/invitations/components/CopyLinkButton';
import { InvitationLinkCard } from '@/features/property-mngt/invitations/components/InvitationLinkCard';
import { useCreateInvitation } from '@/features/property-mngt/invitations/hooks/useCreateInvitation';
import { useInvitation } from '@/features/property-mngt/invitations/hooks/useInvitation';
import type { PartyLot } from '@/features/property-mngt/parties/types/party.types';

interface InviteContactForLotModalProps {
  isOpen: boolean;
  propertyId: string;
  partyId: string;
  contactFullName: string;
  /** Nul pour un contact sans adresse : il n'y a nulle part où envoyer le lien. */
  contactEmail: string | null;
  /** Les lots de ce contact dans cette copropriété, déjà filtrés par l'appelant. */
  lots: PartyLot[];
  /** Vrai quand une invitation court encore : on la remplace, on n'en fait pas courir deux. */
  hasOutstandingInvitation: boolean;
  onClose: () => void;
}

/**
 * L'envoi d'une invitation privée, depuis la fiche du contact et pour l'un de
 * ses lots - le seul endroit d'où elle part désormais. Un lien nominatif n'a
 * de sens qu'ici : c'est le seul écran où l'on sait de qui l'on parle, et
 * pour quel lot.
 *
 * <p>Les lots proposés sont ceux du contact, pas les lots libres de la
 * copropriété : inviter quelqu'un « pour un lot » suppose que le syndic l'a
 * déjà rattaché à ce lot. Un contact sans lot n'est donc pas invitable d'ici,
 * et on le lui dit plutôt que d'ouvrir une liste vide.
 *
 * <p>Le lien s'affiche après l'envoi bien que l'email soit déjà parti (voir
 * CreateInvitationService) : un email se perd dans les indésirables, et le
 * syndic finit souvent par le renvoyer par WhatsApp, en séance.
 *
 * <p>Renvoyer ferme l'invitation qui courait encore (côté serveur, voir
 * CreateInvitationService) : le lien précédent cesse de valoir, et il n'y a
 * jamais deux invitations vivantes pour un même contact.
 */
export function InviteContactForLotModal({
  isOpen,
  propertyId,
  partyId,
  contactFullName,
  contactEmail,
  lots,
  hasOutstandingInvitation,
  onClose,
}: InviteContactForLotModalProps) {
  const [unitId, setUnitId] = useState('');
  const [createdInvitationId, setCreatedInvitationId] = useState<string | null>(null);
  const createInvitation = useCreateInvitation(propertyId);
  const createdInvitation = useInvitation(createdInvitationId);

  function close() {
    setUnitId('');
    setCreatedInvitationId(null);
    createInvitation.reset();
    onClose();
  }

  const selectedLot = lots.find((lot) => lot.unitId === unitId);

  return (
    <Modal
      isOpen={isOpen}
      onClose={close}
      title={hasOutstandingInvitation ? "Renvoyer l'invitation" : 'Inviter ce contact pour un lot'}
      className="max-w-md"
    >
      <div className="flex flex-col gap-4">
        {!contactEmail && (
          <Alert
            variant="warning"
            message="Ce contact n'a pas d'adresse email : renseignez-la sur sa fiche avant de l'inviter."
          />
        )}

        {lots.length === 0 ? (
          <EmptyState title="Aucun lot rattaché à ce contact">
            Une invitation privée porte sur un lot précis. Rattachez d'abord ce contact à l'un des lots de la
            copropriété, depuis la fiche du lot.
          </EmptyState>
        ) : createdInvitationId ? (
          <div className="flex flex-col gap-3">
            <Alert
              variant="success"
              message={`Invitation envoyée à ${contactEmail} pour le lot ${selectedLot?.unitNumber ?? ''}.`}
            />
            {createdInvitation.data && (
              <>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  Vous pouvez aussi lui transmettre ce lien directement :
                </p>
                <p className="break-all text-sm text-gray-700 dark:text-gray-300">{createdInvitation.data.link}</p>
                <div className="flex flex-wrap gap-2">
                  <CopyLinkButton link={createdInvitation.data.link} />
                </div>
                <InvitationLinkCard link={createdInvitation.data.link} />
              </>
            )}
            <div className="flex justify-end">
              <Button type="button" onClick={close}>
                Terminer
              </Button>
            </div>
          </div>
        ) : (
          <>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              {contactFullName} recevra un lien pour créer son compte. Il confirmera le lot ci-dessous, puis sa
              demande reviendra ici pour validation.
            </p>
            {hasOutstandingInvitation && (
              <Alert
                variant="warning"
                message="Une invitation est déjà en cours pour ce contact : l'envoyer à nouveau annulera le lien précédent."
              />
            )}
            {createInvitation.isError && <Alert message={getErrorMessage(createInvitation.error)} />}
            <Select label="Lot concerné" name="unitId" value={unitId} onChange={(e) => setUnitId(e.target.value)}>
              <option value="" disabled>
                Sélectionner un lot
              </option>
              {lots.map((lot) => (
                <option key={lot.unitId} value={lot.unitId}>
                  {lot.buildingName} — {lot.unitNumber}
                </option>
              ))}
            </Select>
            <div className="flex justify-end gap-2">
              <Button type="button" variant="secondary" onClick={close}>
                Annuler
              </Button>
              <Button
                type="button"
                isLoading={createInvitation.isPending}
                disabled={!unitId || !contactEmail}
                onClick={() =>
                  createInvitation.mutate(
                    { propertyId, type: 'PRIVATE', partyId, unitId },
                    { onSuccess: (result) => setCreatedInvitationId(result.id) },
                  )
                }
              >
                {hasOutstandingInvitation ? "Renvoyer l'invitation" : "Envoyer l'invitation"}
              </Button>
            </div>
          </>
        )}
      </div>
    </Modal>
  );
}
