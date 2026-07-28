import { httpClient } from '@/shared/api/httpClient';
import type { PropertyContact } from '@/features/property-mngt/properties/types/property.types';

export async function getPropertyContacts(propertyId: string): Promise<PropertyContact[]> {
  const { data } = await httpClient.get<PropertyContact[]>(`/properties/${propertyId}/contacts`);
  return data;
}
