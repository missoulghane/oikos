import { httpClient } from '@/shared/api/httpClient';
import type { AddBankAccountPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export async function addBankAccount(propertyId: string, payload: AddBankAccountPayload): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/accounting/bank-accounts`, payload);
}
