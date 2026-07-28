import { httpClient } from '@/shared/api/httpClient';
import type { AddUnitOwnershipPayload } from '@/features/property-mngt/properties/types/property.types';

export interface AddUnitOwnershipParams {
  unitId: string;
  payload: AddUnitOwnershipPayload;
}

export async function addUnitOwnership({ unitId, payload }: AddUnitOwnershipParams): Promise<void> {
  await httpClient.post(`/units/${unitId}/owners`, payload);
}
