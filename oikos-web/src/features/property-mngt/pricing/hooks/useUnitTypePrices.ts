import { useQuery } from '@tanstack/react-query';
import { getUnitTypePrices } from '@/features/property-mngt/pricing/api/getUnitTypePrices';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUnitTypePrices(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.unitTypePrices(propertyId),
    queryFn: () => getUnitTypePrices(propertyId),
  });
}
