import { httpClient } from '@/shared/api/httpClient';
import type { DuesCalculationMode, Property } from '@/features/property-mngt/properties/types/property.types';

export async function updateDuesCalculationMode(propertyId: string, mode: DuesCalculationMode): Promise<Property> {
  const { data } = await httpClient.put<Property>(`/properties/${propertyId}/dues-calculation-mode`, { mode });
  return data;
}
