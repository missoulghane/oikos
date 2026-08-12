import { useQuery } from '@tanstack/react-query';
import { getUnreadNotificationCount } from '@/features/notifications/api/getUnreadNotificationCount';
import { queryKeys } from '@/shared/constants/queryKeys';

// Same polling rationale as messaging's useUnreadSummary: no WebSocket/SSE channel yet, so the
// unread badge is kept fresh by re-fetching on an interval rather than pushed.
const POLL_INTERVAL_MS = 30_000;

export function useUnreadNotificationCount() {
  return useQuery({
    queryKey: queryKeys.notifications.unreadCount(),
    queryFn: getUnreadNotificationCount,
    refetchInterval: POLL_INTERVAL_MS,
  });
}
