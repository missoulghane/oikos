import { useMutation, useQueryClient } from '@tanstack/react-query';
import { addUnit } from '@/features/property-mngt/properties/api/addUnit';
import type { AddUnitPayload } from '@/features/property-mngt/properties/types/property.types';

export function useAddUnit(buildingId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: AddUnitPayload) => addUnit({ buildingId, payload }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['buildings', buildingId, 'units'] });
    },
  });
}
