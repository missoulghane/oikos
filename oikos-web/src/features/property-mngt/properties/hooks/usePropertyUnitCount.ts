import { useQuery } from '@tanstack/react-query';
import { getPropertyUnitCount } from '@/features/property-mngt/properties/api/getPropertyUnitCount';
import { queryKeys } from '@/shared/constants/queryKeys';

export function usePropertyUnitCount(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.unitCount(propertyId),
    queryFn: () => getPropertyUnitCount(propertyId),
    enabled: Boolean(propertyId),
  });
}
