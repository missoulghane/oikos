import { useQuery } from '@tanstack/react-query';
import { getParty } from '@/features/property-mngt/parties/api/getParty';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useParty(partyId: string) {
  return useQuery({
    queryKey: queryKeys.parties.detail(partyId),
    queryFn: () => getParty(partyId),
  });
}
