import { useQuery } from '@tanstack/react-query';
import { getPublicInvitation } from '@/features/property-mngt/invitations/api/getPublicInvitation';
import { queryKeys } from '@/shared/constants/queryKeys';

export function usePublicInvitation(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.invitations.publicLink(propertyId),
    queryFn: () => getPublicInvitation(propertyId),
  });
}
