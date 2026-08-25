import { useQuery } from '@tanstack/react-query';
import { getMyNotifications } from '@/features/notifications/api/getMyNotifications';
import { queryKeys } from '@/shared/constants/queryKeys';

/** unreadOnly : ce que liste la cloche du header, filtré côté serveur - une page
 *  de 5 filtrée en mémoire n'en afficherait parfois que 2. */
export function useMyNotifications(page: number, size: number, unreadOnly = false) {
  return useQuery({
    queryKey: queryKeys.notifications.list(page, size, unreadOnly),
    queryFn: () => getMyNotifications(page, size, unreadOnly),
  });
}
