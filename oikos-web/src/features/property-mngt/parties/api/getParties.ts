import { httpClient } from '@/shared/api/httpClient';
import type { PagedParties } from '@/features/property-mngt/parties/types/party.types';

export interface GetPartiesParams {
  propertyId: string;
  page: number;
  size: number;
  search?: string;
}

export async function getParties({ propertyId, page, size, search }: GetPartiesParams): Promise<PagedParties> {
  const { data } = await httpClient.get<PagedParties>('/parties', { params: { propertyId, page, size, search } });
  return data;
}
