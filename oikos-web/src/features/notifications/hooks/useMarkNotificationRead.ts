import { useMutation, useQueryClient } from '@tanstack/react-query';
import { markNotificationRead } from '@/features/notifications/api/markNotificationRead';

export function useMarkNotificationRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (notificationId: string) => markNotificationRead(notificationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
}
