import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { BoardInvitationRow } from '@/features/property-mngt/board-members/components/BoardInvitationRow';
import type { Invitation } from '@/features/property-mngt/invitations/types/invitation.types';

interface BoardInvitationListProps {
  propertyId: string;
  invitations: Invitation[];
}

export function BoardInvitationList({ propertyId, invitations }: BoardInvitationListProps) {
  if (invitations.length === 0) {
    return <EmptyState title="Aucune invitation de bureau en cours" />;
  }

  return (
    <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
      {invitations.map((invitation) => (
        <BoardInvitationRow key={invitation.id} propertyId={propertyId} invitation={invitation} />
      ))}
    </ul>
  );
}
