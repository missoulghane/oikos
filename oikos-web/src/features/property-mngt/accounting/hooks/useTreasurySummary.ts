import { useQuery } from '@tanstack/react-query';
import { getTreasurySummary } from '@/features/property-mngt/accounting/api/getTreasurySummary';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useTreasurySummary(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.accountingTreasurySummary(propertyId),
    queryFn: () => getTreasurySummary(propertyId),
  });
}
