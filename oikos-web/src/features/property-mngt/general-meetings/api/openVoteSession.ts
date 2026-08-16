import { httpClient } from '@/shared/api/httpClient';
import type { AgendaItem } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

export async function openVoteSession(agendaItemId: string): Promise<AgendaItem> {
  const { data } = await httpClient.post<AgendaItem>(`/agenda-items/${agendaItemId}/vote-session/open`);
  return data;
}
