import { useMutation, useQueryClient } from '@tanstack/react-query';
import { markConversationRead } from '@/features/messaging/api/markConversationRead';
import { queryKeys } from '@/shared/constants/queryKeys';

/** Pendant de useMarkConversationUnread pour la liste : l'identifiant vient du clic, pas du montage. */
export function useMarkConversationReadById() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (conversationId: string) => markConversationRead(conversationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messaging', 'conversations'] });
      queryClient.invalidateQueries({ queryKey: queryKeys.messaging.unreadSummary() });
    },
  });
}
