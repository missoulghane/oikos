import { httpClient } from '@/shared/api/httpClient';
import type { PagedUnitAccountMovements } from '@/features/property-mngt/accounting/types/accounting.types';

export interface GetUnitAccountMovementsParams {
  unitId: string;
  page: number;
  size: number;
}

export async function getUnitAccountMovements({
  unitId,
  page,
  size,
}: GetUnitAccountMovementsParams): Promise<PagedUnitAccountMovements> {
  const { data } = await httpClient.get<PagedUnitAccountMovements>(`/units/${unitId}/account/movements`, {
    params: { page, size },
  });
  return data;
}
