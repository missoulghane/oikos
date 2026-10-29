import { useQuery } from '@tanstack/react-query';
import { getBoardRoles } from '@/shared/api/getBoardRoles';
import { queryKeys } from '@/shared/constants/queryKeys';

/** Mis en cache pour la session : le catalogue ne change presque jamais. */
export function useBoardRoles() {
  return useQuery({
    queryKey: queryKeys.reference.boardRoles(),
    queryFn: getBoardRoles,
    staleTime: Infinity,
    // Plus patient que le défaut (1) : sans ce catalogue, l'écran qui le propose
    // ne peut rien faire - autant laisser passer un redémarrage de l'API.
    retry: 3,
  });
}

/**
 * Le libellé d'un siège. Retombe sur le code tant que le catalogue n'est pas
 * chargé, plutôt que de laisser un badge vide.
 */
export function useBoardRoleLabel(): (code: string) => string {
  const { data } = useBoardRoles();
  return (code) => data?.find((role) => role.code === code)?.label ?? code;
}
