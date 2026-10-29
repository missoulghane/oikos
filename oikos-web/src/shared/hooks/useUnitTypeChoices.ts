import { useQuery } from '@tanstack/react-query';
import { getUnitTypeChoices } from '@/shared/api/getUnitTypeChoices';
import { queryKeys } from '@/shared/constants/queryKeys';

/** Mis en cache pour la session : le catalogue ne change presque jamais. */
export function useUnitTypeChoices() {
  return useQuery({
    queryKey: queryKeys.reference.unitTypeChoices(),
    queryFn: getUnitTypeChoices,
    staleTime: Infinity,
    // Plus patient que le défaut (1) : sans ce catalogue, l'écran qui le propose
    // ne peut rien faire - autant laisser passer un redémarrage de l'API.
    retry: 3,
  });
}
