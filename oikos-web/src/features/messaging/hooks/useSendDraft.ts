import { useMutation, useQueryClient } from '@tanstack/react-query';
import { sendDraft } from '@/features/messaging/api/sendDraft';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useSendDraft(draftId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => sendDraft(draftId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messaging', 'drafts'] });
      queryClient.invalidateQueries({ queryKey: ['messaging', 'conversations'] });
      queryClient.invalidateQueries({ queryKey: queryKeys.messaging.unreadSummary() });
    },
  });
}
