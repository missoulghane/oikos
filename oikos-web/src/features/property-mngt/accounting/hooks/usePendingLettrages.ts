import { useQuery } from '@tanstack/react-query';
import { listPendingLettrages } from '@/features/property-mngt/accounting/api/listPendingLettrages';
import { queryKeys } from '@/shared/constants/queryKeys';

export function usePendingLettrages(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.accountingPendingLettrages(propertyId),
    queryFn: () => listPendingLettrages(propertyId),
  });
}
