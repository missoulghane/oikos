import { useMutation } from '@tanstack/react-query';
import { recordPayrollExpense } from '@/features/property-mngt/accounting/api/recordPayrollExpense';
import type { RecordPayrollExpensePayload } from '@/features/property-mngt/accounting/types/accounting.types';

export function useRecordPayrollExpense(propertyId: string) {
  return useMutation({
    mutationFn: (payload: RecordPayrollExpensePayload) => recordPayrollExpense(propertyId, payload),
  });
}
