import { httpClient } from '@/shared/api/httpClient';
import type { PartyDetail } from '@/features/property-mngt/parties/types/party.types';

export async function updatePartyPhone(partyId: string, phone: string | undefined): Promise<PartyDetail> {
  const { data } = await httpClient.patch<PartyDetail>(`/parties/${partyId}/phone`, { phone });
  return data;
}
