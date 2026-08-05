import { useQuery } from '@tanstack/react-query';
import { getUnitAccountMovements } from '@/features/property-mngt/accounting/api/getUnitAccountMovements';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 5;

export function useUnitAccountMovements(unitId: string, page: number, size: number = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: queryKeys.units.accountMovements(unitId, page, size),
    queryFn: () => getUnitAccountMovements({ unitId, page, size }),
    placeholderData: (previousData) => previousData,
  });
}
