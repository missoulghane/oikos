import { useQuery } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { getOpenExercise } from '@/features/property-mngt/accounting/api/getOpenExercise';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useOpenExercise(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.accountingOpenExercise(propertyId),
    queryFn: () => getOpenExercise(propertyId),
    // A 400 here means "no open exercise yet" (NoOpenExerciseException) - a
    // normal, expected state for a property that hasn't opened one yet, not
    // worth retrying.
    retry: (failureCount, error) => !isAxiosError(error) && failureCount < 3,
  });
}
