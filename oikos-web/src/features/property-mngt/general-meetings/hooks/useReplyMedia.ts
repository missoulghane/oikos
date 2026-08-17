import { useQuery } from '@tanstack/react-query';
import { listReplyMedia } from '@/features/property-mngt/general-meetings/api/listReplyMedia';
import { queryKeys } from '@/shared/constants/queryKeys';

/**
 * Cached for the session like the channel catalog, and for the same reason: it
 * barely changes, and a row added server-side still appears on the next load.
 */
export function useReplyMedia() {
  return useQuery({
    queryKey: queryKeys.generalMeetings.replyMedia(),
    queryFn: listReplyMedia,
    staleTime: Infinity,
  });
}
