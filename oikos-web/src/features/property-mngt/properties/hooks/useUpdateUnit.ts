import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateUnit } from '@/features/property-mngt/properties/api/updateUnit';
import type { UpdateUnitPayload } from '@/features/property-mngt/properties/types/property.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUpdateUnit(unitId: string, buildingId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: UpdateUnitPayload) => updateUnit(unitId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.units.detail(unitId) });
      // Number and floor are columns of the building's lot list.
      queryClient.invalidateQueries({ queryKey: ['buildings', buildingId, 'units'] });
    },
  });
}
