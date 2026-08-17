import { httpClient } from '@/shared/api/httpClient';
import type { ReplyMedium } from '@/features/property-mngt/general-meetings/types/convocation.types';

/**
 * The means by which an answer can reach the office, in display order. Fetched
 * rather than hardcoded: the server keeps the catalog precisely so adding one
 * needs no release here.
 */
export async function listReplyMedia(): Promise<ReplyMedium[]> {
  const { data } = await httpClient.get<ReplyMedium[]>('/reply-media');
  return data;
}
