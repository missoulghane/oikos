import { useQuery } from '@tanstack/react-query';
import { getLatestPayment } from '@/features/property-mngt/installments/api/getLatestPayment';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useLatestPayment(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.latestPayment(propertyId),
    queryFn: () => getLatestPayment(propertyId),
  });
}
