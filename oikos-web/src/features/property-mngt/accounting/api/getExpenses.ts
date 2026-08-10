import { httpClient } from '@/shared/api/httpClient';
import type { Expense } from '@/features/property-mngt/accounting/types/accounting.types';

export async function getExpenses(propertyId: string): Promise<Expense[]> {
  const { data } = await httpClient.get<Expense[]>(`/properties/${propertyId}/accounting/expenses`);
  return data;
}
