import { httpClient } from '@/shared/api/httpClient';
import type { PagedUnits, UnitListFilters } from '@/features/property-mngt/properties/types/property.types';

export interface GetUnitsParams extends UnitListFilters {
  buildingId: string;
  page: number;
  size: number;
}

export async function getUnits({
  buildingId,
  page,
  size,
  search,
  ownershipStatus,
  sortBy,
  sortDirection,
}: GetUnitsParams): Promise<PagedUnits> {
  const { data } = await httpClient.get<PagedUnits>(`/buildings/${buildingId}/units`, {
    params: { page, size, search, ownershipStatus, sortBy, sortDirection },
  });
  return data;
}
