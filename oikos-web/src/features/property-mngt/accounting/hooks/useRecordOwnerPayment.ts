import { useMutation, useQueryClient } from '@tanstack/react-query';
import { recordOwnerPayment } from '@/features/property-mngt/accounting/api/recordOwnerPayment';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { RecordOwnerPaymentPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export function useRecordOwnerPayment(propertyId: string, unitId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: RecordOwnerPaymentPayload) => recordOwnerPayment(propertyId, unitId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.units.account(unitId) });
      // Prefix match (no page/size) invalidates every loaded page of movements.
      queryClient.invalidateQueries({ queryKey: ['units', unitId, 'account', 'movements'] });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingTreasurySummary(propertyId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingFinancialAccounts(propertyId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingUnitsSummary(propertyId) });
    },
  });
}
