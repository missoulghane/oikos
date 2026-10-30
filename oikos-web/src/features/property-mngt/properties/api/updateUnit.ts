import { httpClient } from '@/shared/api/httpClient';
import type { Unit, UpdateUnitPayload } from '@/features/property-mngt/properties/types/property.types';

export async function updateUnit(id: string, payload: UpdateUnitPayload): Promise<Unit> {
  const { data } = await httpClient.put<Unit>(`/units/${id}`, payload);
  return data;
}
