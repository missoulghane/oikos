import { useQuery } from '@tanstack/react-query';
import { getInvitationPreview } from '@/features/identity/invitations/api/getInvitationPreview';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useInvitationPreview(token: string | null) {
  return useQuery({
    queryKey: queryKeys.invitations.preview(token ?? ''),
    queryFn: () => getInvitationPreview(token as string),
    enabled: Boolean(token),
    retry: false,
  });
}
