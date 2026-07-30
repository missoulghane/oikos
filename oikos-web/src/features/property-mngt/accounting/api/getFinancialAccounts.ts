import { httpClient } from '@/shared/api/httpClient';
import type { FinancialAccount } from '@/features/property-mngt/accounting/types/accounting.types';

export async function getFinancialAccounts(propertyId: string): Promise<FinancialAccount[]> {
  const { data } = await httpClient.get<FinancialAccount[]>(`/properties/${propertyId}/accounting/financial-accounts`);
  return data;
}
