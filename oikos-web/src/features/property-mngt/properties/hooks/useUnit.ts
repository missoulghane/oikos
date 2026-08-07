import { useQuery } from '@tanstack/react-query';
import { getUnit } from '@/features/property-mngt/properties/api/getUnit';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUnit(id: string | null | undefined) {
  return useQuery({
    queryKey: queryKeys.units.detail(id ?? ''),
    queryFn: () => getUnit(id as string),
    enabled: Boolean(id),
  });
}
