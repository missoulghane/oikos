import { httpClient } from '@/shared/api/httpClient';
import type {
  ConversationBox,
  ConversationReadState,
  PagedConversations,
} from '@/features/messaging/types/messaging.types';

export interface ListMyConversationsParams {
  page: number;
  size: number;
  search?: string;
  box: ConversationBox;
  /** Lu / non lu ; omis = les deux. Filtré côté serveur, avant la pagination. */
  readState?: ConversationReadState;
}

export async function listMyConversations({
  page,
  size,
  search,
  box,
  readState,
}: ListMyConversationsParams): Promise<PagedConversations> {
  const { data } = await httpClient.get<PagedConversations>('/users/me/conversations', {
    params: { page, size, search, box, readState },
  });
  return data;
}
