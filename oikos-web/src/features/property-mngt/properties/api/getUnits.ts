import { httpClient } from '@/shared/api/httpClient';
import type { PagedUnits } from '@/features/property-mngt/properties/types/property.types';

export interface GetUnitsParams {
  buildingId: string;
  page: number;
  size: number;
  search?: string;
}

export async function getUnits({ buildingId, page, size, search }: GetUnitsParams): Promise<PagedUnits> {
  const { data } = await httpClient.get<PagedUnits>(`/buildings/${buildingId}/units`, {
    params: { page, size, search },
  });
  return data;
}
