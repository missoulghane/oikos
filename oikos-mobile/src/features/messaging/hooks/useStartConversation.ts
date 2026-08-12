import { useMutation, useQueryClient } from '@tanstack/react-query';
import { startConversation } from '@/features/messaging/api/startConversation';
import type { StartConversationPayload } from '@/features/messaging/types/messaging.types';

export function useStartConversation(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: StartConversationPayload) => startConversation(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messaging', 'conversations'] });
    },
  });
}
