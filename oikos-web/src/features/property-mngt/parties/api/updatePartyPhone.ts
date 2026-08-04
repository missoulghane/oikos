import { httpClient } from '@/shared/api/httpClient';
import type { Party } from '@/features/property-mngt/parties/types/party.types';

export async function updatePartyPhone(partyId: string, phone: string | undefined): Promise<Party> {
  const { data } = await httpClient.patch<Party>(`/parties/${partyId}/phone`, { phone });
  return data;
}
