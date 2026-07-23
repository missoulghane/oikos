import { httpClient } from '@/shared/api/httpClient';
import type { UnitOwnership } from '@/features/property-mngt/properties/types/property.types';

export async function getUnitOwners(unitId: string): Promise<UnitOwnership[]> {
  const { data } = await httpClient.get<UnitOwnership[]>(`/units/${unitId}/owners`);
  return data;
}
