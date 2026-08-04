import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateDuesCalculationMode } from '@/features/property-mngt/pricing/api/updateDuesCalculationMode';
import type { DuesCalculationMode } from '@/features/property-mngt/properties/types/property.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUpdateDuesCalculationMode(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (mode: DuesCalculationMode) => updateDuesCalculationMode(propertyId, mode),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.detail(propertyId) });
    },
  });
}
