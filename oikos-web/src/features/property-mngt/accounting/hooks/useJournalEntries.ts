import { useQuery } from '@tanstack/react-query';
import { getJournalEntries } from '@/features/property-mngt/accounting/api/getJournalEntries';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 10;

export function useJournalEntries(propertyId: string, page: number, size: number = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: queryKeys.properties.accountingJournalEntries(propertyId, page, size),
    queryFn: () => getJournalEntries({ propertyId, page, size }),
    placeholderData: (previousData) => previousData,
  });
}
