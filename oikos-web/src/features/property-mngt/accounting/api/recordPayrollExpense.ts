import { httpClient } from '@/shared/api/httpClient';
import type {
  JournalEntryReference,
  RecordPayrollExpensePayload,
} from '@/features/property-mngt/accounting/types/accounting.types';

export async function recordPayrollExpense(
  propertyId: string,
  payload: RecordPayrollExpensePayload,
): Promise<JournalEntryReference> {
  const { data } = await httpClient.post<JournalEntryReference>(
    `/properties/${propertyId}/accounting/payroll-expenses`,
    payload,
  );
  return data;
}
