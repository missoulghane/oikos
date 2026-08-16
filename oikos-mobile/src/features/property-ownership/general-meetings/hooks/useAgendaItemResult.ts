import { useQuery } from '@tanstack/react-query';
import { getAgendaItemResult } from '@/features/property-ownership/general-meetings/api/getAgendaItemResult';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useAgendaItemResult(agendaItemId: string, enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.result(agendaItemId),
    queryFn: () => getAgendaItemResult(agendaItemId),
    enabled,
  });
}
