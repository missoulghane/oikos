import { httpClient } from '@/shared/api/httpClient';
import type { AddUnitOwnerPayload } from '@/features/property-mngt/properties/types/property.types';

export interface AddUnitOwnerParams {
  unitId: string;
  payload: AddUnitOwnerPayload;
}

export async function addUnitOwner({ unitId, payload }: AddUnitOwnerParams): Promise<void> {
  await httpClient.post(`/units/${unitId}/owners/new-party`, payload);
}
