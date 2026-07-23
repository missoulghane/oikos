import { httpClient } from '@/shared/api/httpClient';
import type { Unit } from '@/features/property-mngt/properties/types/property.types';

export async function getUnit(id: string): Promise<Unit> {
  const { data } = await httpClient.get<Unit>(`/units/${id}`);
  return data;
}
