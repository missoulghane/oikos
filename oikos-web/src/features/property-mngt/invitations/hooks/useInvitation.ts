import { useQuery } from '@tanstack/react-query';
import { getInvitation } from '@/features/property-mngt/invitations/api/getInvitation';
import { queryKeys } from '@/shared/constants/queryKeys';

/** `id` nul tant qu'aucune invitation n'a été créée : la requête ne part pas. */
export function useInvitation(id: string | null) {
  return useQuery({
    queryKey: queryKeys.invitations.detail(id ?? ''),
    queryFn: () => getInvitation(id as string),
    enabled: Boolean(id),
  });
}
