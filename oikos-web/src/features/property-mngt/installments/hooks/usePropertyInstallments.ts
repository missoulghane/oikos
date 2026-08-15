import { useQuery } from '@tanstack/react-query';
import { getPropertyInstallments } from '@/features/property-mngt/installments/api/getPropertyInstallments';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { InstallmentListFilters } from '@/features/property-mngt/installments/types/installment.types';

const DEFAULT_PAGE_SIZE = 5;

export function usePropertyInstallments(
  propertyId: string,
  page: number,
  filters: InstallmentListFilters,
  size: number = DEFAULT_PAGE_SIZE,
) {
  return useQuery({
    queryKey: queryKeys.properties.installments(propertyId, page, size, filters),
    queryFn: () => getPropertyInstallments({ propertyId, page, size, ...filters }),
    placeholderData: (previousData) => previousData,
  });
}
