import { useQuery } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { getMeetingMinutes } from '@/features/property-mngt/general-meetings/api/getMeetingMinutes';
import { queryKeys } from '@/shared/constants/queryKeys';

/**
 * A meeting with no minutes yet answers 404, which is a normal state and not
 * an error to retry: the screen shows the "generate" action instead.
 */
export function useMeetingMinutes(meetingId: string) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.minutes(meetingId),
    queryFn: () => getMeetingMinutes(meetingId),
    enabled: meetingId !== '',
    retry: (failureCount, error) => !isNotFound(error) && failureCount < 2,
  });
}

export function isNotFound(error: unknown): boolean {
  return isAxiosError(error) && error.response?.status === 404;
}
