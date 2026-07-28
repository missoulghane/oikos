import { httpClient } from '@/shared/api/httpClient';
import type { Party } from '@/features/property-mngt/parties/types/party.types';

export async function getParty(partyId: string): Promise<Party> {
  const { data } = await httpClient.get<Party>(`/parties/${partyId}`);
  return data;
}
