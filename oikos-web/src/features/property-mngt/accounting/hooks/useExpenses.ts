import { useQuery } from '@tanstack/react-query';
import { getExpenses } from '@/features/property-mngt/accounting/api/getExpenses';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useExpenses(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.accountingExpenses(propertyId),
    queryFn: () => getExpenses(propertyId),
  });
}
