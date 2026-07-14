import { httpClient } from '@/shared/api/httpClient';
import type { Property } from '@/features/properties/types/property.types';

export async function getProperty(id: string): Promise<Property> {
  const { data } = await httpClient.get<Property>(`/properties/${id}`);
  return data;
}
