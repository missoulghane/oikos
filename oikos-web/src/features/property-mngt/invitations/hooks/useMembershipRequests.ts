import { useQuery } from '@tanstack/react-query';
import { getMembershipRequests } from '@/features/property-mngt/invitations/api/getMembershipRequests';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 20;

export function useMembershipRequests(propertyId: string, page: number, size: number = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: queryKeys.invitations.membershipRequests(propertyId, page, size),
    queryFn: () => getMembershipRequests({ propertyId, page, size }),
    placeholderData: (previousData) => previousData,
  });
}
