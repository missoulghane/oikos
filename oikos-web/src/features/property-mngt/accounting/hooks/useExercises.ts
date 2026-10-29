import { useQuery } from '@tanstack/react-query';
import { getExercises } from '@/features/property-mngt/accounting/api/getExercises';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useExercises(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.accountingExercises(propertyId),
    queryFn: () => getExercises(propertyId),
  });
}
