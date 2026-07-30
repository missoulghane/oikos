import { useQuery } from '@tanstack/react-query';
import { getFinancialJournal } from '@/features/property-mngt/accounting/api/getFinancialJournal';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { JournalFilters } from '@/features/property-mngt/accounting/types/accounting.types';

const DEFAULT_PAGE_SIZE = 20;

export function useFinancialJournal(
  propertyId: string,
  page: number,
  filters: JournalFilters,
  size: number = DEFAULT_PAGE_SIZE,
) {
  return useQuery({
    queryKey: queryKeys.properties.accountingJournal(
      propertyId,
      page,
      size,
      filters.financialAccountId,
      filters.type,
      filters.dateFrom,
      filters.dateTo,
    ),
    queryFn: () => getFinancialJournal({ propertyId, page, size, ...filters }),
    placeholderData: (previousData) => previousData,
  });
}
