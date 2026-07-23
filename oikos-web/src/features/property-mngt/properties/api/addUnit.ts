import { httpClient } from '@/shared/api/httpClient';
import type { AddUnitPayload } from '@/features/property-mngt/properties/types/property.types';

export interface AddUnitParams {
  buildingId: string;
  payload: AddUnitPayload;
}

export async function addUnit({ buildingId, payload }: AddUnitParams): Promise<void> {
  await httpClient.post(`/buildings/${buildingId}/units`, payload);
}
