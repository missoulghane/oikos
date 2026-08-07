import { useQuery } from '@tanstack/react-query';
import { getMyMembershipRequests } from '@/features/property-ownership/membership-requests/api/getMyMembershipRequests';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useMyMembershipRequests() {
  return useQuery({
    queryKey: queryKeys.me.membershipRequests(),
    queryFn: getMyMembershipRequests,
  });
}
