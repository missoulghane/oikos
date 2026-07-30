import { useMutation, useQueryClient } from '@tanstack/react-query';
import { transferBetweenFinancialAccounts } from '@/features/property-mngt/accounting/api/transferBetweenFinancialAccounts';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { TransferBetweenFinancialAccountsPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export function useTransferBetweenFinancialAccounts(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: TransferBetweenFinancialAccountsPayload) =>
      transferBetweenFinancialAccounts(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingFinancialAccounts(propertyId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingTreasurySummary(propertyId) });
    },
  });
}
