import { httpClient } from '@/shared/api/httpClient';
import type {
  Property,
  UpdatePropertyPayload,
} from '@/features/property-mngt/properties/types/property.types';

export async function updateProperty(id: string, payload: UpdatePropertyPayload): Promise<Property> {
  const { data } = await httpClient.put<Property>(`/properties/${id}`, payload);
  return data;
}
