import { httpClient } from '@/shared/api/httpClient';
import type { PagedMessageDrafts } from '@/features/messaging/types/messaging.types';

export interface ListMyDraftsParams {
  page: number;
  size: number;
  search?: string;
}

export async function listMyDrafts({ page, size, search }: ListMyDraftsParams): Promise<PagedMessageDrafts> {
  const { data } = await httpClient.get<PagedMessageDrafts>('/users/me/message-drafts', {
    params: { page, size, search },
  });
  return data;
}
