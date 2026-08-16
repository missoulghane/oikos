import { useQuery } from '@tanstack/react-query';
import { listConvocations } from '@/features/property-mngt/general-meetings/api/listConvocations';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useConvocations(meetingId: string) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.convocations(meetingId),
    queryFn: () => listConvocations(meetingId),
    enabled: meetingId !== '',
  });
}
