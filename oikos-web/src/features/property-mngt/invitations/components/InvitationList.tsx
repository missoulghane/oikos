import { Pagination } from '@/shared/components/Pagination/Pagination';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { InvitationRow } from '@/features/property-mngt/invitations/components/InvitationRow';
import type { PagedInvitations } from '@/features/property-mngt/invitations/types/invitation.types';

interface InvitationListProps {
  propertyId: string;
  data: PagedInvitations;
  onPageChange: (page: number) => void;
}

export function InvitationList({ propertyId, data, onPageChange }: InvitationListProps) {
  if (data.content.length === 0) {
    return (
      <EmptyState title="Aucune invitation pour le moment">
        Créez une invitation pour inviter un futur copropriétaire.
      </EmptyState>
    );
  }

  return (
    <div className="flex flex-col gap-3">
      <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
        {data.content.map((invitation) => (
          <InvitationRow key={invitation.id} propertyId={propertyId} invitation={invitation} />
        ))}
      </ul>
      <Pagination pageNumber={data.pageNumber} totalPages={data.totalPages} onPageChange={onPageChange} />
    </div>
  );
}
