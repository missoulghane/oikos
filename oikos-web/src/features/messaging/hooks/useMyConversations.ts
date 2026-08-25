import { useQuery } from '@tanstack/react-query';
import { listMyConversations } from '@/features/messaging/api/listMyConversations';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { ConversationBox, ConversationReadState } from '@/features/messaging/types/messaging.types';

/** Cinq par page, réception comme envoi : une boîte se parcourt, elle ne se déroule pas. */
const DEFAULT_PAGE_SIZE = 5;
// The inbox is the closest thing this app has to a notification stream while
// mounted - short polling, no WebSocket/SSE in this repo (see plan §Contexte).
const POLL_INTERVAL_MS = 25_000;

export function useMyConversations(
  page: number,
  box: ConversationBox,
  search?: string,
  readState?: ConversationReadState,
  size: number = DEFAULT_PAGE_SIZE,
) {
  return useQuery({
    queryKey: queryKeys.messaging.conversations(page, size, search, box, readState),
    queryFn: () => listMyConversations({ page, size, search, box, readState }),
    placeholderData: (previousData) => previousData,
    refetchInterval: POLL_INTERVAL_MS,
  });
}
