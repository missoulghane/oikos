import { httpClient } from '@/shared/api/httpClient';
import type { AddBuildingPayload } from '@/features/property-mngt/properties/types/property.types';

export interface AddBuildingParams {
  propertyId: string;
  payload: AddBuildingPayload;
}

export async function addBuilding({ propertyId, payload }: AddBuildingParams): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/buildings`, payload);
}
