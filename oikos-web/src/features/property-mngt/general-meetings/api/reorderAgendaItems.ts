import { httpClient } from '@/shared/api/httpClient';
import type { AgendaItem } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

/** The whole agenda in its new order, never a delta - the API refuses a partial list. */
export async function reorderAgendaItems(meetingId: string, orderedItemIds: string[]): Promise<AgendaItem[]> {
  const { data } = await httpClient.put<AgendaItem[]>(`/general-meetings/${meetingId}/agenda-items/order`, {
    orderedItemIds,
  });
  return data;
}
