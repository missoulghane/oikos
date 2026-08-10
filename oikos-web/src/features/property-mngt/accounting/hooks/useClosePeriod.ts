import { useMutation, useQueryClient } from '@tanstack/react-query';
import { closePeriod } from '@/features/property-mngt/accounting/api/closePeriod';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useClosePeriod(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (period: string) => closePeriod(propertyId, period),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingOpenExercise(propertyId) });
    },
  });
}
