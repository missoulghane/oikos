import { useQuery } from '@tanstack/react-query';
import { getMyNotifications } from '@/features/notifications/api/getMyNotifications';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useMyNotifications(page: number, size: number) {
  return useQuery({
    queryKey: queryKeys.notifications.list(page, size),
    queryFn: () => getMyNotifications(page, size),
  });
}
