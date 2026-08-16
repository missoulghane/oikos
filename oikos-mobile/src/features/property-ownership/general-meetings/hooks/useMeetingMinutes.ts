import { useQuery } from '@tanstack/react-query';
import axios from 'axios';
import { getMeetingMinutes } from '@/features/property-ownership/general-meetings/api/getMeetingMinutes';
import { queryKeys } from '@/shared/constants/queryKeys';

/** Not-yet-drafted minutes answer 404 - a normal state here, not an error to retry. */
export function useMeetingMinutes(meetingId: string) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.minutes(meetingId),
    queryFn: () => getMeetingMinutes(meetingId),
    enabled: meetingId !== '',
    retry: (failureCount, error) => !isNotFound(error) && failureCount < 2,
  });
}

export function isNotFound(error: unknown): boolean {
  return axios.isAxiosError(error) && error.response?.status === 404;
}
