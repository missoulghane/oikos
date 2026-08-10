import { httpClient } from '@/shared/api/httpClient';
import type { OwnershipStatus, PagedUnits } from '@/features/property-mngt/properties/types/property.types';

export interface GetUnitsParams {
  buildingId: string;
  page: number;
  size: number;
  search?: string;
  ownershipStatus?: OwnershipStatus;
}

export async function getUnits({ buildingId, page, size, search, ownershipStatus }: GetUnitsParams): Promise<PagedUnits> {
  const { data } = await httpClient.get<PagedUnits>(`/buildings/${buildingId}/units`, {
    params: { page, size, search, ownershipStatus },
  });
  return data;
}
