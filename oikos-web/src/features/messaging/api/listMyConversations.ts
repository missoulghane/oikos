import { httpClient } from '@/shared/api/httpClient';
import type { ConversationBox, PagedConversations } from '@/features/messaging/types/messaging.types';

export interface ListMyConversationsParams {
  page: number;
  size: number;
  search?: string;
  box: ConversationBox;
}

export async function listMyConversations({
  page,
  size,
  search,
  box,
}: ListMyConversationsParams): Promise<PagedConversations> {
  const { data } = await httpClient.get<PagedConversations>('/users/me/conversations', {
    params: { page, size, search, box },
  });
  return data;
}
