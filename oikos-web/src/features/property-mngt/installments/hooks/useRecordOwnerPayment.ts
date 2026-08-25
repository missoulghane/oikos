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
      // Prefix match: every cached page/filter combo of the property-wide list,
      // plus the "à collecter" summary that nests under the same key. A recette
      // settles echeances that show there, and the API imputes the lot's
      // remaining advance on top of it (PaymentAdvanceRegularizationListener),
      // so more lines than the ones this payment paid for can have moved.
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'installments'] });
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
