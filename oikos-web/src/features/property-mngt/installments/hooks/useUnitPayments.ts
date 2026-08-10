import { useQuery } from '@tanstack/react-query';
import { getUnitPayments } from '@/features/property-mngt/installments/api/getUnitPayments';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUnitPayments(unitId: string) {
  return useQuery({
    queryKey: queryKeys.units.payments(unitId),
    queryFn: () => getUnitPayments(unitId),
  });
}
