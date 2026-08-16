import { httpClient } from '@/shared/api/httpClient';
import type { AgendaItem } from '@/features/property-ownership/general-meetings/types/generalMeeting.types';

export async function listAgendaItems(meetingId: string): Promise<AgendaItem[]> {
  const { data } = await httpClient.get<AgendaItem[]>(`/general-meetings/${meetingId}/agenda-items`);
  return data;
}
