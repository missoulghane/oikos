import { httpClient } from '@/shared/api/httpClient';
import type { Vote } from '@/features/property-mngt/general-meetings/types/vote.types';

export async function listVotes(agendaItemId: string): Promise<Vote[]> {
  const { data } = await httpClient.get<Vote[]>(`/agenda-items/${agendaItemId}/votes`);
  return data;
}
