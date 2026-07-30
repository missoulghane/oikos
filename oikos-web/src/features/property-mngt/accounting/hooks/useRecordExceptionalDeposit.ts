import { useMutation, useQueryClient } from '@tanstack/react-query';
import { recordExceptionalDeposit } from '@/features/property-mngt/accounting/api/recordExceptionalDeposit';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { RecordExceptionalDepositPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export function useRecordExceptionalDeposit(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: RecordExceptionalDepositPayload) => recordExceptionalDeposit(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingFinancialAccounts(propertyId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingTreasurySummary(propertyId) });
    },
  });
}
