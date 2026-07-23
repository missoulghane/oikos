import { httpClient } from '@/shared/api/httpClient';

export interface AddUnitTypeDefinitionParams {
  propertyId: string;
  name: string;
}

export async function addUnitTypeDefinition({ propertyId, name }: AddUnitTypeDefinitionParams): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/unit-types`, { name });
}
