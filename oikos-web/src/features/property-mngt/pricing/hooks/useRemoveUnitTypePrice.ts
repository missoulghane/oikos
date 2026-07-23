import { useMutation, useQueryClient } from '@tanstack/react-query';
import { removeUnitTypePrice } from '@/features/property-mngt/pricing/api/removeUnitTypePrice';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useRemoveUnitTypePrice(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (unitTypeId: string) => removeUnitTypePrice({ propertyId, unitTypeId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.unitTypePrices(propertyId) });
    },
  });
}
