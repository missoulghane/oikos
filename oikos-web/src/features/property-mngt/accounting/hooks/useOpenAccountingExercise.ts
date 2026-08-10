import { useMutation, useQueryClient } from '@tanstack/react-query';
import { openExercise } from '@/features/property-mngt/accounting/api/openExercise';
import type { OpenExercisePayload } from '@/features/property-mngt/accounting/types/accounting.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useOpenAccountingExercise(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: OpenExercisePayload) => openExercise(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingOpenExercise(propertyId) });
    },
  });
}
