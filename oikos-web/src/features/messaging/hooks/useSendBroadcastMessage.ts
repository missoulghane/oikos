import { useMutation, useQueryClient } from '@tanstack/react-query';
import { sendBroadcastMessage } from '@/features/messaging/api/sendBroadcastMessage';
import type { SendBroadcastMessagePayload } from '@/features/messaging/types/messaging.types';

export function useSendBroadcastMessage(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: SendBroadcastMessagePayload) => sendBroadcastMessage(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messaging', 'conversations'] });
    },
  });
}
