import { useMutation, useQueryClient } from '@tanstack/react-query';
import { regularizeUnitAdvance } from '@/features/property-mngt/installments/api/regularizeUnitAdvance';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useRegularizeUnitAdvance(propertyId: string, unitId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => regularizeUnitAdvance(propertyId, unitId),
    onSuccess: () => {
      // Same reach as a recette, minus the payments: échéances settled, an OD entry posted.
      queryClient.invalidateQueries({ queryKey: queryKeys.units.installments(unitId) });
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'installments'] });
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'accounting'] });
    },
  });
}
