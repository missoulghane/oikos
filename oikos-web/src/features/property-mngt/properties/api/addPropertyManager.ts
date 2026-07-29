import { httpClient } from '@/shared/api/httpClient';

export interface AddPropertyManagerParams {
  propertyId: string;
  email: string;
}

export async function addPropertyManager({ propertyId, email }: AddPropertyManagerParams): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/managers`, { email });
}
