import { useQuery } from '@tanstack/react-query';
import { getFinancialAccounts } from '@/features/property-mngt/accounting/api/getFinancialAccounts';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useFinancialAccounts(propertyId: string, enabled: boolean = true) {
  return useQuery({
    queryKey: queryKeys.properties.accountingFinancialAccounts(propertyId),
    queryFn: () => getFinancialAccounts(propertyId),
    enabled,
  });
}
