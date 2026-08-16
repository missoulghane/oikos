import { useQuery } from '@tanstack/react-query';
import { getGeneralMeeting } from '@/features/property-ownership/general-meetings/api/getGeneralMeeting';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useGeneralMeeting(meetingId: string) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.detail(meetingId),
    queryFn: () => getGeneralMeeting(meetingId),
    enabled: meetingId !== '',
  });
}
