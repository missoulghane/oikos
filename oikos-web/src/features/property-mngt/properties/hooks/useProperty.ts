import { useQuery } from '@tanstack/react-query';
import { getProperty } from '@/features/property-mngt/properties/api/getProperty';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useProperty(id: string) {
  return useQuery({
    queryKey: queryKeys.properties.detail(id),
    queryFn: () => getProperty(id),
  });
}
