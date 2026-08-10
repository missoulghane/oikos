import { useQuery } from '@tanstack/react-query';
import { getLedgerAccounts } from '@/features/property-mngt/accounting/api/getLedgerAccounts';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useLedgerAccounts(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.accountingLedgerAccounts(propertyId),
    queryFn: () => getLedgerAccounts(propertyId),
  });
}
