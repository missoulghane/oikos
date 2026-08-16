import { useQuery } from '@tanstack/react-query';
import { getAttendanceSummary } from '@/features/property-mngt/general-meetings/api/getAttendanceSummary';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useAttendanceSummary(meetingId: string) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.attendanceSummary(meetingId),
    queryFn: () => getAttendanceSummary(meetingId),
    enabled: meetingId !== '',
  });
}
