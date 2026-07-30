import { httpClient } from '@/shared/api/httpClient';

export async function validateUnitLettrage(propertyId: string, unitId: string): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/accounting/units/${unitId}/lettrage/validate`);
}
