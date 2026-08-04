import { httpClient } from '@/shared/api/httpClient';
import type { Unit, UpdateUnitSharesPayload } from '@/features/property-mngt/properties/types/property.types';

export async function updateUnitShares(id: string, payload: UpdateUnitSharesPayload): Promise<Unit> {
  const { data } = await httpClient.put<Unit>(`/units/${id}/shares`, payload);
  return data;
}
