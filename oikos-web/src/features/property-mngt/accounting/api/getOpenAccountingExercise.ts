import { httpClient } from '@/shared/api/httpClient';
import type { AccountingExercise } from '@/features/property-mngt/accounting/types/accounting.types';

export async function getOpenAccountingExercise(propertyId: string): Promise<AccountingExercise> {
  const { data } = await httpClient.get<AccountingExercise>(`/properties/${propertyId}/accounting/exercises/open`);
  return data;
}
