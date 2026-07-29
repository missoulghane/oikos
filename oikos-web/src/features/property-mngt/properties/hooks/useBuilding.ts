import { useQuery } from '@tanstack/react-query';
import { getBuilding } from '@/features/property-mngt/properties/api/getBuilding';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useBuilding(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.buildings.detail(id as string),
    queryFn: () => getBuilding(id as string),
    enabled: Boolean(id),
  });
}
