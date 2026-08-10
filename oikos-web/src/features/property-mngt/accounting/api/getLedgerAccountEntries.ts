import { httpClient } from '@/shared/api/httpClient';
import type {
  JournalEntryListFilters,
  PagedJournalEntries,
} from '@/features/property-mngt/accounting/types/accounting.types';

export interface GetLedgerAccountEntriesParams extends JournalEntryListFilters {
  propertyId: string;
  accountId: string;
  page: number;
  size: number;
}

export async function getLedgerAccountEntries({
  propertyId,
  accountId,
  page,
  size,
  pieceDateFrom,
  pieceDateTo,
  search,
  status,
}: GetLedgerAccountEntriesParams): Promise<PagedJournalEntries> {
  const { data } = await httpClient.get<PagedJournalEntries>(
    `/properties/${propertyId}/accounting/ledger-accounts/${accountId}/entries`,
    { params: { page, size, pieceDateFrom, pieceDateTo, search, status } },
  );
  return data;
}
