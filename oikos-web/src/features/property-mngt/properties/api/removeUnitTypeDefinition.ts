import { httpClient } from '@/shared/api/httpClient';

export interface RemoveUnitTypeDefinitionParams {
  propertyId: string;
  unitTypeId: string;
}

export async function removeUnitTypeDefinition({ propertyId, unitTypeId }: RemoveUnitTypeDefinitionParams): Promise<void> {
  await httpClient.delete(`/properties/${propertyId}/unit-types/${unitTypeId}`);
}
