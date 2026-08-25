import { httpClient } from '@/shared/api/httpClient';
import type { RecipientGroup, SaveRecipientGroupPayload } from '@/features/messaging/types/messaging.types';

export async function listRecipientGroups(propertyId: string): Promise<RecipientGroup[]> {
  const { data } = await httpClient.get<RecipientGroup[]>(`/properties/${propertyId}/messaging/groups`);
  return data;
}

export async function createRecipientGroup(propertyId: string, payload: SaveRecipientGroupPayload): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/messaging/groups`, payload);
}

export async function updateRecipientGroup(
  propertyId: string,
  groupId: string,
  payload: SaveRecipientGroupPayload,
): Promise<void> {
  await httpClient.put(`/properties/${propertyId}/messaging/groups/${groupId}`, payload);
}

export async function deleteRecipientGroup(propertyId: string, groupId: string): Promise<void> {
  await httpClient.delete(`/properties/${propertyId}/messaging/groups/${groupId}`);
}
