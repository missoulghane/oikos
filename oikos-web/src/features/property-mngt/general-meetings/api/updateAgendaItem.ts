import { httpClient } from '@/shared/api/httpClient';
import type { AgendaItem, AgendaItemPayload } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

export async function updateAgendaItem(agendaItemId: string, payload: AgendaItemPayload): Promise<AgendaItem> {
  const { data } = await httpClient.put<AgendaItem>(`/agenda-items/${agendaItemId}`, payload);
  return data;
}
