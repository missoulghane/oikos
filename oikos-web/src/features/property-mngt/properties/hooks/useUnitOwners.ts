import { useQuery } from '@tanstack/react-query';
import { getUnitOwners } from '@/features/property-mngt/properties/api/getUnitOwners';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUnitOwners(unitId: string) {
  return useQuery({
    queryKey: queryKeys.units.owners(unitId),
    queryFn: () => getUnitOwners(unitId),
  });
}
