import { httpClient } from '@/shared/api/httpClient';
import type { TreasurySummary } from '@/features/property-mngt/accounting/types/accounting.types';

export async function getTreasurySummary(propertyId: string): Promise<TreasurySummary> {
  const { data } = await httpClient.get<TreasurySummary>(`/properties/${propertyId}/accounting/treasury-summary`);
  return data;
}
