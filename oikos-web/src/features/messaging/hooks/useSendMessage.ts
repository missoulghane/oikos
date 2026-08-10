import { useMutation, useQueryClient } from '@tanstack/react-query';
import { sendMessage } from '@/features/messaging/api/sendMessage';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { SendMessagePayload } from '@/features/messaging/types/messaging.types';

export function useSendMessage(conversationId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: SendMessagePayload) => sendMessage(conversationId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.messaging.messages(conversationId) });
      queryClient.invalidateQueries({ queryKey: ['messaging', 'conversations'] });
    },
  });
}
