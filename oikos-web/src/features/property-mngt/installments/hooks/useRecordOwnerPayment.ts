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
    },
  });
}
