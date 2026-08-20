import { useMutation, useQueryClient } from '@tanstack/react-query';
import { reopenPeriod } from '@/features/property-mngt/accounting/api/reopenPeriod';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useReopenPeriod(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (period: string) => reopenPeriod(propertyId, period),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingOpenExercise(propertyId) });
    },
  });
}
