import { useQuery } from '@tanstack/react-query';
import { listConversationMessages } from '@/features/messaging/api/listConversationMessages';
import { queryKeys } from '@/shared/constants/queryKeys';

const POLL_INTERVAL_MS = 7_000;

export function useConversationMessages(conversationId: string) {
  return useQuery({
    queryKey: queryKeys.messaging.messages(conversationId),
    queryFn: () => listConversationMessages(conversationId),
    refetchInterval: POLL_INTERVAL_MS,
  });
}
