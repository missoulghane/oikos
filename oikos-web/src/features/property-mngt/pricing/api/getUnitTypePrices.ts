import { httpClient } from '@/shared/api/httpClient';
import type { UnitTypePrice } from '@/features/property-mngt/pricing/types/pricing.types';

export async function getUnitTypePrices(propertyId: string): Promise<UnitTypePrice[]> {
  const { data } = await httpClient.get<UnitTypePrice[]>(`/properties/${propertyId}/unit-type-prices`);
  return data;
}
