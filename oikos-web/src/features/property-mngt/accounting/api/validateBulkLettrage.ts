import { httpClient } from '@/shared/api/httpClient';
import type { ValidateBulkLettragePayload } from '@/features/property-mngt/accounting/types/accounting.types';

export async function validateBulkLettrage(propertyId: string, payload: ValidateBulkLettragePayload = {}): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/accounting/lettrage/validate`, payload);
}
