import { useQuery } from '@tanstack/react-query';
import { listConvocationChannels } from '@/features/property-mngt/general-meetings/api/listConvocationChannels';
import { queryKeys } from '@/shared/constants/queryKeys';

/**
 * The catalog barely changes, so it is cached for the session rather than
 * refetched with every screen that offers a channel. A row added server-side
 * still appears on the next load - which is the point of it being data.
 */
export function useConvocationChannels() {
  return useQuery({
    queryKey: queryKeys.generalMeetings.convocationChannels(),
    queryFn: listConvocationChannels,
    staleTime: Infinity,
  });
}
