import { useQuery } from '@tanstack/react-query';
import { getInstallmentCollectionSummary } from '@/features/property-mngt/installments/api/getInstallmentCollectionSummary';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useInstallmentCollectionSummary(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.installmentCollectionSummary(propertyId),
    queryFn: () => getInstallmentCollectionSummary(propertyId),
    enabled: Boolean(propertyId),
  });
}
