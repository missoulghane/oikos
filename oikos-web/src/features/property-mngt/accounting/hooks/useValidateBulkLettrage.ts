import { useMutation, useQueryClient } from '@tanstack/react-query';
import { validateBulkLettrage } from '@/features/property-mngt/accounting/api/validateBulkLettrage';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { ValidateBulkLettragePayload } from '@/features/property-mngt/accounting/types/accounting.types';

export function useValidateBulkLettrage(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: ValidateBulkLettragePayload = {}) => validateBulkLettrage(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingPendingLettrages(propertyId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingUnitsSummary(propertyId) });
    },
  });
}
