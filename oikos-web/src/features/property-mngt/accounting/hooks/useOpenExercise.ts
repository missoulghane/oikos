import { useMutation, useQueryClient } from '@tanstack/react-query';
import { openAccountingExercise } from '@/features/property-mngt/accounting/api/openAccountingExercise';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { OpenExercisePayload } from '@/features/property-mngt/accounting/types/accounting.types';

export function useOpenExercise(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: OpenExercisePayload) => openAccountingExercise(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingOpenExercise(propertyId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingTreasurySummary(propertyId) });
    },
  });
}
