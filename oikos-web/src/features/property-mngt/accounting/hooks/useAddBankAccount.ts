import { useMutation, useQueryClient } from '@tanstack/react-query';
import { addBankAccount } from '@/features/property-mngt/accounting/api/addBankAccount';
import type { AddBankAccountPayload } from '@/features/property-mngt/accounting/types/accounting.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useAddBankAccount(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: AddBankAccountPayload) => addBankAccount(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingLedgerAccounts(propertyId) });
    },
  });
}
