import { httpClient } from '@/shared/api/httpClient';
import type { SaveMessageDraftPayload } from '@/features/messaging/types/messaging.types';

export async function updateDraft(draftId: string, payload: SaveMessageDraftPayload): Promise<void> {
  await httpClient.put(`/message-drafts/${draftId}`, payload);
}
