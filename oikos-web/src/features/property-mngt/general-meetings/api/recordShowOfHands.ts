import { httpClient } from '@/shared/api/httpClient';
import type { AgendaItemResult, ShowOfHandsPayload } from '@/features/property-mngt/general-meetings/types/vote.types';

/** One choice for the lots present, with named exceptions - the API applies it to the room only. */
export async function recordShowOfHands(
  agendaItemId: string,
  payload: ShowOfHandsPayload,
): Promise<AgendaItemResult> {
  const { data } = await httpClient.post<AgendaItemResult>(
    `/agenda-items/${agendaItemId}/votes/show-of-hands`,
    payload,
  );
  return data;
}
