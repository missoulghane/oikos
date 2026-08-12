import { useMutation, useQueryClient } from '@tanstack/react-query';
import { markConversationRead } from '@/features/messaging/api/markConversationRead';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useMarkConversationRead(conversationId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => markConversationRead(conversationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messaging', 'conversations'] });
      queryClient.invalidateQueries({ queryKey: queryKeys.messaging.unreadSummary() });
    },
  });
}
