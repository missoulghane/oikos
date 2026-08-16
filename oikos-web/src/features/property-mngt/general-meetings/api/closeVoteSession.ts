import { httpClient } from '@/shared/api/httpClient';
import type { AgendaItemResult } from '@/features/property-mngt/general-meetings/types/vote.types';

/** Returns the result as of closing - the figures the chair announces to the room. */
export async function closeVoteSession(agendaItemId: string): Promise<AgendaItemResult> {
  const { data } = await httpClient.post<AgendaItemResult>(`/agenda-items/${agendaItemId}/vote-session/close`);
  return data;
}
