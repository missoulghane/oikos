import { useQuery } from '@tanstack/react-query';
import { getPartyLots } from '@/features/property-mngt/parties/api/getPartyLots';
import { queryKeys } from '@/shared/constants/queryKeys';

export function usePartyLots(partyId: string) {
  return useQuery({
    queryKey: queryKeys.parties.lots(partyId),
    queryFn: () => getPartyLots(partyId),
  });
}
