import { useMutation, useQueryClient } from '@tanstack/react-query';
import { recordOwnerPayment } from '@/features/property-mngt/installments/api/recordOwnerPayment';
import type { RecordOwnerPaymentPayload } from '@/features/property-mngt/installments/types/payment.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useRecordOwnerPayment(propertyId: string, unitId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: RecordOwnerPaymentPayload) => recordOwnerPayment(propertyId, unitId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.units.payments(unitId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.units.installments(unitId) });
      // Prefix match: also invalidates every treasury account's operations list
      // (queryKeys.properties.ledgerAccountEntries nests under this same key),
      // so a "saisir une recette" from an account's page shows the new entry
      // and updated balance immediately on return, not stale cached data.
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingLedgerAccounts(propertyId) });
      // Prefix match (no size): invalidates every configured "N latest payments" cache entry.
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'payments', 'latest'] });
    },
  });
}
