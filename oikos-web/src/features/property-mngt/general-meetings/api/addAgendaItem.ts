import { httpClient } from '@/shared/api/httpClient';
import type { AgendaItem, AgendaItemPayload } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

export async function addAgendaItem(meetingId: string, payload: AgendaItemPayload): Promise<AgendaItem> {
  const { data } = await httpClient.post<AgendaItem>(`/general-meetings/${meetingId}/agenda-items`, payload);
  return data;
}
