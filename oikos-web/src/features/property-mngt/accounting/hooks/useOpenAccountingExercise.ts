import { useQuery } from '@tanstack/react-query';
import { getOpenAccountingExercise } from '@/features/property-mngt/accounting/api/getOpenAccountingExercise';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useOpenAccountingExercise(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.accountingOpenExercise(propertyId),
    queryFn: () => getOpenAccountingExercise(propertyId),
    retry: false,
  });
}
