import { useQuery } from '@tanstack/react-query';
import { getLatestPayments } from '@/features/property-mngt/installments/api/getLatestPayments';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useLatestPayments(propertyId: string, size: number) {
  return useQuery({
    queryKey: queryKeys.properties.latestPayments(propertyId, size),
    queryFn: () => getLatestPayments(propertyId, size),
  });
}
