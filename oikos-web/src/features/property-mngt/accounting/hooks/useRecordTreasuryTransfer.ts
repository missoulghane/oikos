import { useMutation, useQueryClient } from '@tanstack/react-query';
import { recordTreasuryTransfer } from '@/features/property-mngt/accounting/api/recordTreasuryTransfer';
import type { RecordTreasuryTransferPayload } from '@/features/property-mngt/accounting/types/accounting.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useRecordTreasuryTransfer(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: RecordTreasuryTransferPayload) => recordTreasuryTransfer(propertyId, payload),
    onSuccess: () => {
      // Prefix match: also invalidates every treasury account's operations list
      // (queryKeys.properties.ledgerAccountEntries nests under this same key) -
      // a transfer changes the balance of both the source and destination accounts.
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingLedgerAccounts(propertyId) });
    },
  });
}
