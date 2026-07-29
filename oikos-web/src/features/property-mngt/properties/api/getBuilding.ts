import { httpClient } from '@/shared/api/httpClient';
import type { Building } from '@/features/property-mngt/properties/types/property.types';

export async function getBuilding(id: string): Promise<Building> {
  const { data } = await httpClient.get<Building>(`/buildings/${id}`);
  return data;
}
