import { useQuery } from '@tanstack/react-query';
import { getUnreadSummary } from '@/features/messaging/api/getUnreadSummary';
import { queryKeys } from '@/shared/constants/queryKeys';

// Always mounted (Home's Messages button badge) - this is the app's main
// "notification" mechanism in the absence of a WebSocket/SSE channel.
const POLL_INTERVAL_MS = 30_000;

export function useUnreadSummary() {
  return useQuery({
    queryKey: queryKeys.messaging.unreadSummary(),
    queryFn: getUnreadSummary,
    refetchInterval: POLL_INTERVAL_MS,
  });
}
