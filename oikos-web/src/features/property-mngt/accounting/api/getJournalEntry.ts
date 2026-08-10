import { httpClient } from '@/shared/api/httpClient';
import type { JournalEntry } from '@/features/property-mngt/accounting/types/accounting.types';

export async function getJournalEntry(propertyId: string, entryId: string): Promise<JournalEntry> {
  const { data } = await httpClient.get<JournalEntry>(`/properties/${propertyId}/accounting/entries/${entryId}`);
  return data;
}
