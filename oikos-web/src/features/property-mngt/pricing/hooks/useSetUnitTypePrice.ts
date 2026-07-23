import { useMutation, useQueryClient } from '@tanstack/react-query';
import { setUnitTypePrice } from '@/features/property-mngt/pricing/api/setUnitTypePrice';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useSetUnitTypePrice(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ unitTypeId, price }: { unitTypeId: string; price: number }) =>
      setUnitTypePrice({ propertyId, unitTypeId, price }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.unitTypePrices(propertyId) });
    },
  });
}
