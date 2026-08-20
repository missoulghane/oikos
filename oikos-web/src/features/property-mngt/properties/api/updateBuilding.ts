import { httpClient } from '@/shared/api/httpClient';
import type { Building, UpdateBuildingPayload } from '@/features/property-mngt/properties/types/property.types';

export interface UpdateBuildingParams {
  buildingId: string;
  payload: UpdateBuildingPayload;
}

export async function updateBuilding({ buildingId, payload }: UpdateBuildingParams): Promise<Building> {
  const { data } = await httpClient.put<Building>(`/buildings/${buildingId}`, payload);
  return data;
}
