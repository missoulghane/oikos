import { useQuery } from '@tanstack/react-query';
import { getUnitLettrageProposal } from '@/features/property-mngt/accounting/api/getUnitLettrageProposal';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUnitLettrageProposal(propertyId: string, unitId: string, enabled: boolean = true) {
  return useQuery({
    queryKey: queryKeys.units.lettrageProposal(unitId),
    queryFn: () => getUnitLettrageProposal(propertyId, unitId),
    enabled,
  });
}
