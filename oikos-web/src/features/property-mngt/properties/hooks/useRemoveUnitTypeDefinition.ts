import { useMutation, useQueryClient } from '@tanstack/react-query';
import { removeUnitTypeDefinition } from '@/features/property-mngt/properties/api/removeUnitTypeDefinition';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useRemoveUnitTypeDefinition(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (unitTypeId: string) => removeUnitTypeDefinition({ propertyId, unitTypeId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.unitTypes(propertyId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.unitTypePrices(propertyId) });
    },
  });
}
