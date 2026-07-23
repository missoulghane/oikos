import { useQuery } from '@tanstack/react-query';
import { getUnitTypeDefinitions } from '@/features/property-mngt/properties/api/getUnitTypeDefinitions';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUnitTypeDefinitions(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.unitTypes(propertyId),
    queryFn: () => getUnitTypeDefinitions(propertyId),
  });
}
