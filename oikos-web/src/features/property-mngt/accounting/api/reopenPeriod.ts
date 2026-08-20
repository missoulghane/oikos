import { httpClient } from '@/shared/api/httpClient';
import type { Period } from '@/features/property-mngt/accounting/types/accounting.types';

/** period is "yyyy-MM" (mirrors YearMonth.toString() on the backend). */
export async function reopenPeriod(propertyId: string, period: string): Promise<Period> {
  const { data } = await httpClient.post<Period>(`/properties/${propertyId}/accounting/periods/${period}/reopen`);
  return data;
}
