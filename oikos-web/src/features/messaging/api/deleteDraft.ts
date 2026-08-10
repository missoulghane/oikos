import { httpClient } from '@/shared/api/httpClient';

export async function deleteDraft(draftId: string): Promise<void> {
  await httpClient.delete(`/message-drafts/${draftId}`);
}
