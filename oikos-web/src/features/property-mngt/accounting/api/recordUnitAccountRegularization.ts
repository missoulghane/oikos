import { httpClient } from '@/shared/api/httpClient';
import type { RecordUnitAccountRegularizationPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export async function recordUnitAccountRegularization(
  propertyId: string,
  unitId: string,
  payload: RecordUnitAccountRegularizationPayload,
): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/accounting/units/${unitId}/regularizations`, payload);
}
