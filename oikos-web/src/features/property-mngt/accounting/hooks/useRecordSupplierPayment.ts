import { useMutation, useQueryClient } from '@tanstack/react-query';
import { recordSupplierPayment } from '@/features/property-mngt/accounting/api/recordSupplierPayment';
import type { RecordSupplierPaymentPayload } from '@/features/property-mngt/accounting/types/accounting.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useRecordSupplierPayment(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: RecordSupplierPaymentPayload) => recordSupplierPayment(propertyId, payload),
    onSuccess: () => {
      // Prefix match: also invalidates every treasury account's operations list
      // (queryKeys.properties.ledgerAccountEntries nests under this same key),
      // so a "saisir une dépense" from an account's page shows the new entry
      // and updated balance immediately on return, not stale cached data.
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingLedgerAccounts(propertyId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingExpenses(propertyId) });
    },
  });
}
