import { httpClient } from '@/shared/api/httpClient';
import type { UnitAccountSummary } from '@/features/property-mngt/accounting/types/accounting.types';

export async function getUnitAccountSummaries(propertyId: string): Promise<UnitAccountSummary[]> {
  const { data } = await httpClient.get<UnitAccountSummary[]>(`/properties/${propertyId}/accounting/units-summary`);
  return data;
}
