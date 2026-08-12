import { useQuery } from '@tanstack/react-query';
import { getInvitationAvailableUnits } from '@/features/identity/invitations/api/getInvitationAvailableUnits';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useInvitationAvailableUnits(token: string | null, enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.invitations.availableUnits(token ?? ''),
    queryFn: () => getInvitationAvailableUnits(token as string),
    enabled: Boolean(token) && enabled,
  });
}
