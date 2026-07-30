import { httpClient } from '@/shared/api/httpClient';
import type {
  JournalFilters,
  PagedFinancialJournalEntries,
} from '@/features/property-mngt/accounting/types/accounting.types';

export interface GetFinancialJournalParams extends JournalFilters {
  propertyId: string;
  page: number;
  size: number;
}

export async function getFinancialJournal({
  propertyId,
  page,
  size,
  financialAccountId,
  type,
  dateFrom,
  dateTo,
}: GetFinancialJournalParams): Promise<PagedFinancialJournalEntries> {
  const { data } = await httpClient.get<PagedFinancialJournalEntries>(`/properties/${propertyId}/accounting/journal`, {
    params: { page, size, financialAccountId, type, dateFrom, dateTo },
  });
  return data;
}
