import { httpClient } from '@/shared/api/httpClient';
import type { CreateFinancialAccountPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export async function createFinancialAccount(propertyId: string, payload: CreateFinancialAccountPayload): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/accounting/financial-accounts`, payload);
}
