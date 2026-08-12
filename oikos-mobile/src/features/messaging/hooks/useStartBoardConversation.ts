import { useMutation, useQueryClient } from '@tanstack/react-query';
import { startBoardConversation } from '@/features/messaging/api/startBoardConversation';
import type { StartBoardConversationPayload } from '@/features/messaging/types/messaging.types';

export function useStartBoardConversation(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: StartBoardConversationPayload) => startBoardConversation(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messaging', 'conversations'] });
    },
  });
}
