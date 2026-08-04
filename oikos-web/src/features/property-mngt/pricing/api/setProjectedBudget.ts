import { httpClient } from '@/shared/api/httpClient';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export async function setProjectedBudget(propertyId: string, projectedBudget: number): Promise<Property> {
  const { data } = await httpClient.put<Property>(`/properties/${propertyId}/projected-budget`, { projectedBudget });
  return data;
}
