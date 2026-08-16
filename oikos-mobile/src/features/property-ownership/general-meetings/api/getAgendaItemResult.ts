import { httpClient } from '@/shared/api/httpClient';
import type { AgendaItemResult } from '@/features/property-ownership/general-meetings/types/generalMeeting.types';

export async function getAgendaItemResult(agendaItemId: string): Promise<AgendaItemResult> {
  const { data } = await httpClient.get<AgendaItemResult>(`/agenda-items/${agendaItemId}/result`);
  return data;
}
