import { httpClient } from '@/shared/api/httpClient';
import type { InvitePartyResult } from '@/features/property-mngt/parties/types/party.types';

export async function inviteParty(partyId: string): Promise<InvitePartyResult> {
  const { data } = await httpClient.post<InvitePartyResult>(`/parties/${partyId}/invite`);
  return data;
}
