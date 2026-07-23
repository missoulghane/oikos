import { useQuery } from '@tanstack/react-query';
import { getAccountByHolder } from '@/features/property-mngt/accounting/api/getAccountByHolder';
import type { AccountType } from '@/features/property-mngt/accounting/types/accounting.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useAccountByHolder(holderId: string, accountType: AccountType) {
  return useQuery({
    queryKey: queryKeys.accounts.byHolder(holderId, accountType),
    queryFn: () => getAccountByHolder(holderId, accountType),
    retry: false,
  });
}
