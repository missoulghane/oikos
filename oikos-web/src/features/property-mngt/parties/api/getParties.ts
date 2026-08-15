import { httpClient } from '@/shared/api/httpClient';
import type { PagedParties, PartyListFilters } from '@/features/property-mngt/parties/types/party.types';

export interface GetPartiesParams extends PartyListFilters {
  propertyId: string;
  page: number;
  size: number;
}

export async function getParties({
  propertyId,
  page,
  size,
  search,
  sortBy,
  sortDirection,
}: GetPartiesParams): Promise<PagedParties> {
  const { data } = await httpClient.get<PagedParties>('/parties', {
    params: { propertyId, page, size, search, sortBy, sortDirection },
  });
  return data;
}
