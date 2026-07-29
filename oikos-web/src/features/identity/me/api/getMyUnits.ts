import { httpClient } from '@/shared/api/httpClient';
import type { OwnedUnit } from '@/features/identity/me/types/me.types';

export async function getMyUnits(): Promise<OwnedUnit[]> {
  const { data } = await httpClient.get<OwnedUnit[]>('/users/me/units');
  return data;
}
