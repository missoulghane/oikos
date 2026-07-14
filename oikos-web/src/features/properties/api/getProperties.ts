import { httpClient } from '@/shared/api/httpClient';
import type { PagedProperties } from '@/features/properties/types/property.types';

export interface GetPropertiesParams {
  page: number;
  size: number;
}

export async function getProperties({ page, size }: GetPropertiesParams): Promise<PagedProperties> {
  const { data } = await httpClient.get<PagedProperties>('/properties', { params: { page, size } });
  return data;
}
