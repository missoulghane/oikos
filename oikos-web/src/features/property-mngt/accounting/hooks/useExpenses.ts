import { useQuery } from '@tanstack/react-query';
import { getExpenses } from '@/features/property-mngt/accounting/api/getExpenses';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 20;

export function useExpenses(propertyId: string, page: number, size: number = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: queryKeys.properties.accountingExpenses(propertyId, page, size),
    queryFn: () => getExpenses({ propertyId, page, size }),
    placeholderData: (previousData) => previousData,
  });
}
