import { useQuery } from '@tanstack/react-query';
import { getJournalEntry } from '@/features/property-mngt/accounting/api/getJournalEntry';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useJournalEntry(propertyId: string, entryId: string) {
  return useQuery({
    queryKey: queryKeys.properties.accountingJournalEntry(propertyId, entryId),
    queryFn: () => getJournalEntry(propertyId, entryId),
  });
}
