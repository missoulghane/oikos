import { Pagination } from '@/shared/components/Pagination/Pagination';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { MembershipRequestRow } from '@/features/property-mngt/invitations/components/MembershipRequestRow';
import type { PagedMembershipRequests } from '@/features/property-mngt/invitations/types/invitation.types';

interface MembershipRequestListProps {
  propertyId: string;
  data: PagedMembershipRequests;
  onPageChange: (page: number) => void;
}

export function MembershipRequestList({ propertyId, data, onPageChange }: MembershipRequestListProps) {
  if (data.content.length === 0) {
    return (
      <EmptyState title="Aucune candidature pour le moment">
        Les candidatures soumises via le lien public apparaîtront ici.
      </EmptyState>
    );
  }

  return (
    <div className="flex flex-col gap-3">
      <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
        {data.content.map((request) => (
          <MembershipRequestRow key={request.id} propertyId={propertyId} request={request} />
        ))}
      </ul>
      <Pagination pageNumber={data.pageNumber} totalPages={data.totalPages} onPageChange={onPageChange} />
    </div>
  );
}
