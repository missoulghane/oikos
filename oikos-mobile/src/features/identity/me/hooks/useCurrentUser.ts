import { useQuery } from '@tanstack/react-query';
import { getCurrentUser } from '@/features/identity/me/api/getCurrentUser';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useCurrentUser() {
  return useQuery({
    queryKey: queryKeys.me.detail(),
    queryFn: getCurrentUser,
    staleTime: 60_000,
  });
}
