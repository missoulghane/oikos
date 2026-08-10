import { httpClient } from '@/shared/api/httpClient';
import type { PagedJournalEntries } from '@/features/property-mngt/accounting/types/accounting.types';

export interface GetJournalEntriesParams {
  propertyId: string;
  page: number;
  size: number;
}

export async function getJournalEntries({
  propertyId,
  page,
  size,
}: GetJournalEntriesParams): Promise<PagedJournalEntries> {
  const { data } = await httpClient.get<PagedJournalEntries>(`/properties/${propertyId}/accounting/entries`, {
    params: { page, size },
  });
  return data;
}
