import { useQuery } from '@tanstack/react-query';
import { getInvitations } from '@/features/property-mngt/invitations/api/getInvitations';
import { queryKeys } from '@/shared/constants/queryKeys';

// Board invitations are a subset of the property's generic invitations
// (targetRole === 'PROPERTY_BOARD_MEMBER') - there is no dedicated backend
// endpoint for them, so this reuses the same list call and filters
// client-side. The page size is generous since a board rarely has more than
// a handful of outstanding invitations, and this list has no pagination UI.
const PAGE_SIZE = 50;

export function useBoardInvitations(propertyId: string) {
  const query = useQuery({
    queryKey: queryKeys.invitations.list(propertyId, 0, PAGE_SIZE),
    queryFn: () => getInvitations({ propertyId, page: 0, size: PAGE_SIZE }),
  });

  return {
    ...query,
    data: query.data?.content.filter((invitation) => invitation.targetRole === 'PROPERTY_BOARD_MEMBER'),
  };
}
