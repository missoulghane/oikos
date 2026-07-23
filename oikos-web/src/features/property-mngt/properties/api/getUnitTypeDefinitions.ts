import { httpClient } from '@/shared/api/httpClient';
import type { UnitTypeDefinition } from '@/features/property-mngt/properties/types/property.types';

export async function getUnitTypeDefinitions(propertyId: string): Promise<UnitTypeDefinition[]> {
  const { data } = await httpClient.get<UnitTypeDefinition[]>(`/properties/${propertyId}/unit-types`);
  return data;
}
