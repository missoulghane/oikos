import { httpClient } from '@/shared/api/httpClient';
import type { AddBankAccountPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export async function addBankAccount(propertyId: string, payload: AddBankAccountPayload): Promise<string> {
  const response = await httpClient.post(`/properties/${propertyId}/accounting/bank-accounts`, payload);
  const location: string | undefined = response.headers.location;
  return location?.split('/').pop() ?? '';
}
