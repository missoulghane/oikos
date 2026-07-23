import { useQuery } from '@tanstack/react-query';
import { getUnitInstallments } from '@/features/property-mngt/installments/api/getUnitInstallments';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUnitInstallments(unitId: string) {
  return useQuery({
    queryKey: queryKeys.units.installments(unitId),
    queryFn: () => getUnitInstallments(unitId),
  });
}
