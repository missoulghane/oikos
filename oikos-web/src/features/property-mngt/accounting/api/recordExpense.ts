import { httpClient } from '@/shared/api/httpClient';
import type { RecordExpensePayload } from '@/features/property-mngt/accounting/types/accounting.types';

export async function recordExpense(propertyId: string, payload: RecordExpensePayload): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/accounting/expenses`, payload);
}
