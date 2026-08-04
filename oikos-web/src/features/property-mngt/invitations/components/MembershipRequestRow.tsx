import { useState } from 'react';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useParty } from '@/features/property-mngt/parties/hooks/useParty';
import { useUnit } from '@/features/property-mngt/properties/hooks/useUnit';
import { useAcceptMembershipRequest } from '@/features/property-mngt/invitations/hooks/useAcceptMembershipRequest';
import { useRejectMembershipRequest } from '@/features/property-mngt/invitations/hooks/useRejectMembershipRequest';
import type { MembershipRequest, MembershipRequestStatus } from '@/features/property-mngt/invitations/types/invitation.types';

const STATUS_BADGE: Record<MembershipRequestStatus, { label: string; color: 'success' | 'warning' | 'error' }> = {
  PENDING: { label: 'En attente', color: 'warning' },
  ACCEPTED: { label: 'Acceptée', color: 'success' },
  REJECTED: { label: 'Refusée', color: 'error' },
};

interface MembershipRequestRowProps {
  propertyId: string;
  request: MembershipRequest;
}

export function MembershipRequestRow({ propertyId, request }: MembershipRequestRowProps) {
  const party = useParty(request.partyId);
  const unit = useUnit(request.unitId);
  const [isRejecting, setIsRejecting] = useState(false);
  const [reason, setReason] = useState('');
  const acceptRequest = useAcceptMembershipRequest(propertyId);
  const rejectRequest = useRejectMembershipRequest(propertyId);
  const badge = STATUS_BADGE[request.status];

  return (
    <li className="flex flex-col gap-2 px-3 py-2">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div className="min-w-0">
          <p className="flex items-center gap-2 text-sm font-medium text-gray-900">
            {party.data?.fullName ?? 'Candidat…'}
            <Badge color={badge.color}>{badge.label}</Badge>
          </p>
          <p className="truncate text-sm text-gray-500">
            {party.data?.email} — Lot {unit.data?.unitNumber ?? '…'} ({unit.data?.unitTypeName})
          </p>
          {request.status === 'REJECTED' && request.rejectionReason && (
            <p className="truncate text-xs text-gray-400">Motif : {request.rejectionReason}</p>
          )}
        </div>
        {request.status === 'PENDING' && !isRejecting && (
          <div className="flex shrink-0 gap-2">
            <Button
              type="button"
              isLoading={acceptRequest.isPending}
              onClick={() => acceptRequest.mutate(request.id)}
            >
              Accepter
            </Button>
            <Button type="button" variant="secondary" onClick={() => setIsRejecting(true)}>
              Refuser
            </Button>
          </div>
        )}
      </div>

      {acceptRequest.isError && <Alert message={getErrorMessage(acceptRequest.error)} />}

      {isRejecting && (
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
          <div className="flex-1">
            <Input
              label="Motif du refus (optionnel)"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
            />
          </div>
          <div className="flex gap-2">
            <Button
              type="button"
              isLoading={rejectRequest.isPending}
              onClick={() =>
                rejectRequest.mutate(
                  { id: request.id, reason: reason || undefined },
                  { onSuccess: () => setIsRejecting(false) },
                )
              }
            >
              Confirmer le refus
            </Button>
            <Button type="button" variant="secondary" onClick={() => setIsRejecting(false)}>
              Annuler
            </Button>
          </div>
        </div>
      )}
      {rejectRequest.isError && <Alert message={getErrorMessage(rejectRequest.error)} />}
    </li>
  );
}
