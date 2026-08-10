import { httpClient } from '@/shared/api/httpClient';
import type { CreatePartyPayload, CreatePartyResult } from '@/features/property-mngt/parties/types/party.types';

export async function createParty(payload: CreatePartyPayload): Promise<CreatePartyResult> {
  const response = await httpClient.post('/parties', payload);
  const location: string | undefined = response.headers.location;
  const id = location?.split('/').pop() ?? '';
  return { id };
}
