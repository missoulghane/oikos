import { useQuery } from '@tanstack/react-query';
import { listQuorumSettings } from '@/features/property-mngt/general-meetings/api/listQuorumSettings';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useQuorumSettings(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.quorumSettings(propertyId),
    queryFn: () => listQuorumSettings(propertyId),
    enabled: propertyId !== '',
  });
}
