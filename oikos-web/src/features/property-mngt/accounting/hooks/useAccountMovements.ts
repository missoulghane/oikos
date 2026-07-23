import { useQuery } from '@tanstack/react-query';
import { getAccountMovements } from '@/features/property-mngt/accounting/api/getAccountMovements';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 20;

export function useAccountMovements(accountId: string, page: number, enabled = true, size: number = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: queryKeys.accounts.movements(accountId, page, size),
    queryFn: () => getAccountMovements({ accountId, page, size }),
    enabled: enabled && Boolean(accountId),
    placeholderData: (previousData) => previousData,
  });
}
