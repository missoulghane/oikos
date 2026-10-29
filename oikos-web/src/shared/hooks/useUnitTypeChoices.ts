import { useQuery } from '@tanstack/react-query';
import { getUnitTypeChoices } from '@/shared/api/getUnitTypeChoices';
import { queryKeys } from '@/shared/constants/queryKeys';

/** Mis en cache pour la session : le catalogue ne change presque jamais. */
export function useUnitTypeChoices() {
  return useQuery({
    queryKey: queryKeys.reference.unitTypeChoices(),
    queryFn: getUnitTypeChoices,
    staleTime: Infinity,
  });
}
