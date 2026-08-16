import { httpClient } from '@/shared/api/httpClient';
import type { Vote, VoteChoice } from '@/features/property-mngt/general-meetings/types/vote.types';

/** unitId, not partyId: the lot votes, whoever holds it. */
export async function castVote(agendaItemId: string, unitId: string, choice: VoteChoice): Promise<Vote> {
  const { data } = await httpClient.post<Vote>(`/agenda-items/${agendaItemId}/votes`, { unitId, choice });
  return data;
}
