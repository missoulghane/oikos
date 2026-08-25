import { useQuery } from '@tanstack/react-query';
import {
  getMembershipRequests,
  type GetMembershipRequestsParams,
} from '@/features/property-mngt/invitations/api/getMembershipRequests';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 20;

export type MembershipRequestFilters = Omit<GetMembershipRequestsParams, 'propertyId' | 'page' | 'size'>;

export function useMembershipRequests(
  propertyId: string,
  page: number,
  filters: MembershipRequestFilters = {},
  size: number = DEFAULT_PAGE_SIZE,
) {
  return useQuery({
    queryKey: queryKeys.invitations.membershipRequests(propertyId, page, size, filters),
    queryFn: () => getMembershipRequests({ propertyId, page, size, ...filters }),
    placeholderData: (previousData) => previousData,
  });
}
