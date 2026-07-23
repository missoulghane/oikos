import { useQuery } from '@tanstack/react-query';
import { getProperties } from '@/features/property-mngt/properties/api/getProperties';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 20;

export function useProperties(page: number, size: number = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: queryKeys.properties.list(page, size),
    queryFn: () => getProperties({ page, size }),
    placeholderData: (previousData) => previousData,
  });
}
