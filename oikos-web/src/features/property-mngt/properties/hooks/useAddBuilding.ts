import { useMutation, useQueryClient } from '@tanstack/react-query';
import { addBuilding } from '@/features/property-mngt/properties/api/addBuilding';
import type { AddBuildingPayload } from '@/features/property-mngt/properties/types/property.types';

export function useAddBuilding(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: AddBuildingPayload) => addBuilding({ propertyId, payload }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'buildings'] });
    },
  });
}
