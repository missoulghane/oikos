import { useQuery } from '@tanstack/react-query';
import { getUnitAccountSummaries } from '@/features/property-mngt/accounting/api/getUnitAccountSummaries';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUnitAccountSummaries(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.accountingUnitsSummary(propertyId),
    queryFn: () => getUnitAccountSummaries(propertyId),
  });
}
