import { httpClient } from '@/shared/api/httpClient';
import type { PartyLot } from '@/features/property-mngt/parties/types/party.types';

export async function getPartyLots(partyId: string): Promise<PartyLot[]> {
  const { data } = await httpClient.get<PartyLot[]>(`/parties/${partyId}/lots`);
  return data;
}
