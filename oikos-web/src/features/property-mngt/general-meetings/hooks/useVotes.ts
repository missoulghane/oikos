import { useQuery } from '@tanstack/react-query';
import { listVotes } from '@/features/property-mngt/general-meetings/api/listVotes';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useVotes(agendaItemId: string, enabled = true) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.votes(agendaItemId),
    queryFn: () => listVotes(agendaItemId),
    enabled: enabled && agendaItemId !== '',
  });
}
