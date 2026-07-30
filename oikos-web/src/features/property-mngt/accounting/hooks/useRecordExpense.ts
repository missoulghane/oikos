import { useMutation, useQueryClient } from '@tanstack/react-query';
import { recordExpense } from '@/features/property-mngt/accounting/api/recordExpense';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { RecordExpensePayload } from '@/features/property-mngt/accounting/types/accounting.types';

export function useRecordExpense(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: RecordExpensePayload) => recordExpense(propertyId, payload),
    onSuccess: () => {
      // Prefix match (no page/size) invalidates every loaded page.
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'accounting', 'expenses'] });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingTreasurySummary(propertyId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingFinancialAccounts(propertyId) });
    },
  });
}
