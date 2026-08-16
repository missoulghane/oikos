import { useQuery } from '@tanstack/react-query';
import { listAgendaItems } from '@/features/property-ownership/general-meetings/api/listAgendaItems';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useAgendaItems(meetingId: string) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.agendaItems(meetingId),
    queryFn: () => listAgendaItems(meetingId),
    enabled: meetingId !== '',
  });
}
