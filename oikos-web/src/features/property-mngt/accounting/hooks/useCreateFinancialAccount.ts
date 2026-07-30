import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createFinancialAccount } from '@/features/property-mngt/accounting/api/createFinancialAccount';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { CreateFinancialAccountPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export function useCreateFinancialAccount(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CreateFinancialAccountPayload) => createFinancialAccount(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingFinancialAccounts(propertyId) });
    },
  });
}
