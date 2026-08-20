import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateBuilding } from '@/features/property-mngt/properties/api/updateBuilding';
import type { UpdateBuildingPayload } from '@/features/property-mngt/properties/types/property.types';

/**
 * L'invalidation porte sur la copropriété et non sur le seul bâtiment : la liste
 * des lots affiche le nom du bâtiment en en-tête de chaque section, et le
 * renommer sans la rafraîchir laisserait l'ancien nom sous les yeux du syndic
 * qui vient de le corriger.
 */
export function useUpdateBuilding(propertyId: string, buildingId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: UpdateBuildingPayload) => updateBuilding({ buildingId, payload }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'buildings'] });
    },
  });
}
