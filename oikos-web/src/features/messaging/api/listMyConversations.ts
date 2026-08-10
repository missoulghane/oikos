import { httpClient } from '@/shared/api/httpClient';
import type { PagedConversations } from '@/features/messaging/types/messaging.types';

export interface ListMyConversationsParams {
  page: number;
  size: number;
  search?: string;
}

export async function listMyConversations({
  page,
  size,
  search,
}: ListMyConversationsParams): Promise<PagedConversations> {
  const { data } = await httpClient.get<PagedConversations>('/users/me/conversations', {
    params: { page, size, search },
  });
  return data;
}
