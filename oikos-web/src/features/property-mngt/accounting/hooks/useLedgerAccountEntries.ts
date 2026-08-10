import { useQuery } from '@tanstack/react-query';
import { getLedgerAccountEntries } from '@/features/property-mngt/accounting/api/getLedgerAccountEntries';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { JournalEntryListFilters } from '@/features/property-mngt/accounting/types/accounting.types';

export function useLedgerAccountEntries(
  propertyId: string,
  accountId: string,
  page: number,
  filters: JournalEntryListFilters,
  size: number = 20,
) {
  return useQuery({
    queryKey: queryKeys.properties.ledgerAccountEntries(
      propertyId,
      accountId,
      page,
      size,
      filters.pieceDateFrom,
      filters.pieceDateTo,
      filters.search,
      filters.status,
    ),
    queryFn: () =>
      getLedgerAccountEntries({
        propertyId,
        accountId,
        page,
        size,
        pieceDateFrom: filters.pieceDateFrom,
        pieceDateTo: filters.pieceDateTo,
        search: filters.search,
        status: filters.status,
      }),
    placeholderData: (previousData) => previousData,
  });
}
