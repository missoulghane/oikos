import { useMutation, useQueryClient } from '@tanstack/react-query';
import { markConversationUnread } from '@/features/messaging/api/markConversationRead';
import { queryKeys } from '@/shared/constants/queryKeys';

/**
 * Marquer non lu depuis la liste. Prend l'identifiant en argument de la
 * mutation, et non à la construction comme useMarkConversationRead : la liste
 * n'a qu'un hook pour toutes ses lignes.
 */
export function useMarkConversationUnread() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (conversationId: string) => markConversationUnread(conversationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messaging', 'conversations'] });
      queryClient.invalidateQueries({ queryKey: queryKeys.messaging.unreadSummary() });
    },
  });
}
