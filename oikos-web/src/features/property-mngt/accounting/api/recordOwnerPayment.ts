import { httpClient } from '@/shared/api/httpClient';
import type { RecordOwnerPaymentPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export async function recordOwnerPayment(
  propertyId: string,
  unitId: string,
  payload: RecordOwnerPaymentPayload,
): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/accounting/units/${unitId}/payments`, payload);
}
