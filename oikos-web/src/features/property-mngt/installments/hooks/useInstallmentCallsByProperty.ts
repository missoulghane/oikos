import { useQuery } from '@tanstack/react-query';
import { getInstallmentCallsByProperty } from '@/features/property-mngt/installments/api/getInstallmentCallsByProperty';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 20;

export function useInstallmentCallsByProperty(propertyId: string, page: number, size: number = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: queryKeys.properties.installmentCalls(propertyId, page, size),
    queryFn: () => getInstallmentCallsByProperty({ propertyId, page, size }),
    placeholderData: (previousData) => previousData,
  });
}
