import { useState } from 'react';
import { Modal } from '@/shared/components/Modal/Modal';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useRejectMembershipRequest } from '@/features/property-mngt/invitations/hooks/useRejectMembershipRequest';
import type { MembershipRequest } from '@/features/property-mngt/invitations/types/invitation.types';

interface RejectMembershipRequestModalProps {
  propertyId: string;
  /** Nul quand aucune demande n'est en cours de refus : la modale est alors fermée. */
  request: MembershipRequest | null;
  onClose: () => void;
}

/**
 * Le motif du refus part au demandeur, par notification, message, email et
 * WhatsApp (voir MembershipDecisionNotifier côté API) : il est facultatif,
 * mais ce qui est écrit ici est ce que la personne lira.
 *
 * <p>Une modale plutôt qu'un formulaire déplié dans la ligne : dans un tableau,
 * une ligne qui grandit décale tout ce qui la suit au moment précis où le
 * lecteur compare des lignes entre elles.
 */
export function RejectMembershipRequestModal({ propertyId, request, onClose }: RejectMembershipRequestModalProps) {
  const [reason, setReason] = useState('');
  const rejectRequest = useRejectMembershipRequest(propertyId);

  function close() {
    setReason('');
    rejectRequest.reset();
    onClose();
  }

  return (
    <Modal isOpen={request !== null} onClose={close} title="Refuser la demande d'adhésion">
      <div className="flex flex-col gap-4">
        <p className="text-sm text-gray-500 dark:text-gray-400">
          {request?.requesterFullName ?? 'Le demandeur'} sera prévenu du refus et du motif que vous indiquez.
        </p>
        {rejectRequest.isError && <Alert message={getErrorMessage(rejectRequest.error)} />}
        {/* Seul champ, et rien n'y est exigé : pas d'astérisque à contraster,
            l'indication passe donc dans le champ vide plutôt que dans
            l'étiquette - elle disparaît dès la saisie. */}
        <Input
          label="Motif du refus"
          name="rejection-reason"
          placeholder="Facultatif"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
        />
        <div className="flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={close}>
            Annuler
          </Button>
          <Button
            type="button"
            isLoading={rejectRequest.isPending}
            onClick={() =>
              request &&
              rejectRequest.mutate({ id: request.id, reason: reason || undefined }, { onSuccess: close })
            }
          >
            Confirmer le refus
          </Button>
        </div>
      </div>
    </Modal>
  );
}
