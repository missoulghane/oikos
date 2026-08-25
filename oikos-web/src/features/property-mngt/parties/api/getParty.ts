import { httpClient } from '@/shared/api/httpClient';
import type { PartyDetail } from '@/features/property-mngt/parties/types/party.types';

export async function getParty(partyId: string): Promise<PartyDetail> {
  const { data } = await httpClient.get<PartyDetail>(`/parties/${partyId}`);
  return data;
}
