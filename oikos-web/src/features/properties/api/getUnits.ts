import { httpClient } from '@/shared/api/httpClient';
import type { PagedUnits } from '@/features/properties/types/property.types';

export interface GetUnitsParams {
  buildingId: string;
  page: number;
  size: number;
}

export async function getUnits({ buildingId, page, size }: GetUnitsParams): Promise<PagedUnits> {
  const { data } = await httpClient.get<PagedUnits>(`/buildings/${buildingId}/units`, {
    params: { page, size },
  });
  return data;
}
