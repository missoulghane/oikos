import { httpClient } from '@/shared/api/httpClient';
import type { PagedExpenses } from '@/features/property-mngt/accounting/types/accounting.types';

export interface GetExpensesParams {
  propertyId: string;
  page: number;
  size: number;
}

export async function getExpenses({ propertyId, page, size }: GetExpensesParams): Promise<PagedExpenses> {
  const { data } = await httpClient.get<PagedExpenses>(`/properties/${propertyId}/accounting/expenses`, {
    params: { page, size },
  });
  return data;
}
