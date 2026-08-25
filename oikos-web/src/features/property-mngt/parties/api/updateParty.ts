import { httpClient } from '@/shared/api/httpClient';
import type { PartyDetail, UpdatePartyPayload } from '@/features/property-mngt/parties/types/party.types';

export async function updateParty(partyId: string, payload: UpdatePartyPayload): Promise<PartyDetail> {
  const { data } = await httpClient.put<PartyDetail>(`/parties/${partyId}`, payload);
  return data;
}
