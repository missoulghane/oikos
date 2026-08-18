import { httpClient } from '@/shared/api/httpClient';

/** Lots are listable per building only, so their total for a copropriété has its own endpoint. */
export async function getPropertyUnitCount(propertyId: string): Promise<number> {
  const { data } = await httpClient.get<{ count: number }>(`/properties/${propertyId}/units/count`);
  return data.count;
}
