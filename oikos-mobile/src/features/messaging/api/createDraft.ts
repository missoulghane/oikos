import { httpClient } from '@/shared/api/httpClient';
import type {
  MessageDraftReference,
  SaveMessageDraftPayload,
} from '@/features/messaging/types/messaging.types';

export async function createDraft(
  propertyId: string,
  payload: SaveMessageDraftPayload,
): Promise<MessageDraftReference> {
  const { data } = await httpClient.post<MessageDraftReference>(
    `/properties/${propertyId}/message-drafts`,
    payload,
  );
  return data;
}
