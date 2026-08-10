import { useMutation } from '@tanstack/react-query';
import { recordBankCharge } from '@/features/property-mngt/accounting/api/recordBankCharge';
import type { RecordBankChargePayload } from '@/features/property-mngt/accounting/types/accounting.types';

export function useRecordBankCharge(propertyId: string) {
  return useMutation({
    mutationFn: (payload: RecordBankChargePayload) => recordBankCharge(propertyId, payload),
  });
}
