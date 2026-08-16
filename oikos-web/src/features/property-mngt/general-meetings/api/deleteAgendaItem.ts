import { httpClient } from '@/shared/api/httpClient';

export async function deleteAgendaItem(agendaItemId: string): Promise<void> {
  await httpClient.delete(`/agenda-items/${agendaItemId}`);
}
