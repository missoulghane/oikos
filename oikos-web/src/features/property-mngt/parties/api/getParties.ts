import { httpClient } from '@/shared/api/httpClient';
import type { PagedParties } from '@/features/property-mngt/parties/types/party.types';

export interface GetPartiesParams {
  page: number;
  size: number;
  search?: string;
}

export async function getParties({ page, size, search }: GetPartiesParams): Promise<PagedParties> {
  const { data } = await httpClient.get<PagedParties>('/parties', { params: { page, size, search } });
  return data;
}
