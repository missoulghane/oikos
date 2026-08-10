import { useQuery } from '@tanstack/react-query';
import { listMyConversations } from '@/features/messaging/api/listMyConversations';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { ConversationBox } from '@/features/messaging/types/messaging.types';

const DEFAULT_PAGE_SIZE = 20;
// The inbox is the closest thing this app has to a notification stream while
// mounted - short polling, no WebSocket/SSE in this repo (see plan §Contexte).
const POLL_INTERVAL_MS = 25_000;

export function useMyConversations(
  page: number,
  box: ConversationBox,
  search?: string,
  size: number = DEFAULT_PAGE_SIZE,
) {
  return useQuery({
    queryKey: queryKeys.messaging.conversations(page, size, search, box),
    queryFn: () => listMyConversations({ page, size, search, box }),
    placeholderData: (previousData) => previousData,
    refetchInterval: POLL_INTERVAL_MS,
  });
}
