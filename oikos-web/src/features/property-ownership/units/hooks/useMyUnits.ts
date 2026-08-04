import { useQuery } from '@tanstack/react-query';
import { getMyUnits } from '@/features/property-ownership/units/api/getMyUnits';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useMyUnits() {
  return useQuery({
    queryKey: queryKeys.me.units(),
    queryFn: getMyUnits,
  });
}
