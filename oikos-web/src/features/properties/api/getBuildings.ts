import { httpClient } from '@/shared/api/httpClient';
import type { PagedBuildings } from '@/features/properties/types/property.types';

export interface GetBuildingsParams {
  propertyId: string;
  page: number;
  size: number;
}

export async function getBuildings({ propertyId, page, size }: GetBuildingsParams): Promise<PagedBuildings> {
  const { data } = await httpClient.get<PagedBuildings>(`/properties/${propertyId}/buildings`, {
    params: { page, size },
  });
  return data;
}
