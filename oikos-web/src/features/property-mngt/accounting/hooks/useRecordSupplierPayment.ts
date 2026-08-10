import { useMutation } from '@tanstack/react-query';
import { recordSupplierPayment } from '@/features/property-mngt/accounting/api/recordSupplierPayment';
import type { RecordSupplierPaymentPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export function useRecordSupplierPayment(propertyId: string) {
  return useMutation({
    mutationFn: (payload: RecordSupplierPaymentPayload) => recordSupplierPayment(propertyId, payload),
  });
}
