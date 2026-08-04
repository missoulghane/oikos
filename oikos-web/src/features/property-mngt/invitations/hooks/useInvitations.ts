import { useQuery } from '@tanstack/react-query';
import { getInvitations } from '@/features/property-mngt/invitations/api/getInvitations';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 20;

export function useInvitations(propertyId: string, page: number, size: number = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: queryKeys.invitations.list(propertyId, page, size),
    queryFn: () => getInvitations({ propertyId, page, size }),
    placeholderData: (previousData) => previousData,
  });
}
