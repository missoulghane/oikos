import { useState } from 'react';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { CopyLinkButton } from '@/features/property-mngt/invitations/components/CopyLinkButton';
import { InvitationLinkCard } from '@/features/property-mngt/invitations/components/InvitationLinkCard';
import { useDisableInvitation } from '@/features/property-mngt/invitations/hooks/useDisableInvitation';
import type { Invitation, InvitationStatus, InvitationType } from '@/features/property-mngt/invitations/types/invitation.types';

const TYPE_LABELS: Record<InvitationType, string> = {
  PUBLIC: 'Lien public',
  PRIVATE: 'Privée',
};

const STATUS_BADGE: Record<InvitationStatus, { label: string; color: 'success' | 'light' | 'error' }> = {
  ACTIVE: { label: 'Active', color: 'success' },
  DISABLED: { label: 'Désactivée', color: 'light' },
  CONSUMED: { label: 'Utilisée', color: 'light' },
};

interface InvitationRowProps {
  propertyId: string;
  invitation: Invitation;
}

export function InvitationRow({ propertyId, invitation }: InvitationRowProps) {
  const disableInvitation = useDisableInvitation(propertyId);
  const [showQrCode, setShowQrCode] = useState(false);

  return (
    <li className="flex flex-col gap-2 px-3 py-2">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div className="min-w-0">
          <p className="flex items-center gap-2 text-sm font-medium text-gray-900 dark:text-white/90">
            {TYPE_LABELS[invitation.type]}
            <Badge color={STATUS_BADGE[invitation.status].color}>{STATUS_BADGE[invitation.status].label}</Badge>
            {invitation.emailMismatch && (
              <Badge color="warning">⚠ Acceptée par un autre email que celui ciblé</Badge>
            )}
          </p>
          {invitation.targetEmail && <p className="truncate text-sm text-gray-500 dark:text-gray-400">{invitation.targetEmail}</p>}
          <p className="truncate text-xs text-gray-400 dark:text-gray-500">
            Expire le {new Date(invitation.expiresAt).toLocaleDateString('fr-FR')}
          </p>
        </div>
        <div className="flex shrink-0 flex-wrap gap-2">
          <CopyLinkButton link={invitation.link} />
          <Button type="button" variant="secondary" onClick={() => setShowQrCode((value) => !value)}>
            {showQrCode ? 'Masquer le QR code' : 'QR code'}
          </Button>
          {invitation.status === 'ACTIVE' && (
            <Button
              type="button"
              variant="secondary"
              isLoading={disableInvitation.isPending}
              onClick={() => disableInvitation.mutate(invitation.id)}
            >
              Désactiver
            </Button>
          )}
        </div>
      </div>
      {showQrCode && <InvitationLinkCard link={invitation.link} />}
    </li>
  );
}
