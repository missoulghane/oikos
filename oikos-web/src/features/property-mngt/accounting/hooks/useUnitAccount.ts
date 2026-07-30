import { useQuery } from '@tanstack/react-query';
import { getUnitAccount } from '@/features/property-mngt/accounting/api/getUnitAccount';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUnitAccount(unitId: string) {
  return useQuery({
    queryKey: queryKeys.units.account(unitId),
    queryFn: () => getUnitAccount(unitId),
  });
}
