import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { confirmConvocation } from '@/features/property-ownership/general-meetings/api/confirmConvocation';
import { confirmConvocationByCode } from '@/features/property-ownership/general-meetings/api/confirmConvocationByCode';
import { getConvocationConfirmation } from '@/features/property-ownership/general-meetings/api/getConvocationConfirmation';
import { getConvocationConfirmationByCode } from '@/features/property-ownership/general-meetings/api/getConvocationConfirmationByCode';
import type { AttendanceReply } from '@/features/property-mngt/general-meetings/types/convocation.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useConvocationConfirmation(token: string | null) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.convocationConfirmation(token ?? ''),
    queryFn: () => getConvocationConfirmation(token!),
    enabled: Boolean(token),
    // A wrong or revoked link is a 404 that will stay a 404: retrying it only
    // delays the explanation the visitor is waiting for.
    retry: false,
  });
}

export function useConfirmConvocation(token: string | null) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (attendanceReply: AttendanceReply) => confirmConvocation(token!, attendanceReply),
    // The response is the fresh state of the same thing the query holds, so it is
    // written straight in rather than refetched.
    onSuccess: (confirmation) =>
      queryClient.setQueryData(queryKeys.generalMeetings.convocationConfirmation(token ?? ''), confirmation),
  });
}

/**
 * The same convocation, reached by the pair of codes printed on the letter.
 *
 * <p>Not retried, like its token twin, and here it matters more: a wrong pair
 * counts against the server's attempt cap, so retrying it three times spends
 * three of the copropriétaire's own attempts on a typo.
 */
export function useConvocationConfirmationByCode(meetingReference: string, code: string, enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.convocationByCode(meetingReference, code),
    queryFn: () => getConvocationConfirmationByCode(meetingReference, code),
    enabled,
    retry: false,
  });
}

export function useConfirmConvocationByCode(meetingReference: string, code: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (attendanceReply: AttendanceReply) =>
      confirmConvocationByCode(meetingReference, code, attendanceReply),
    onSuccess: (confirmation) =>
      queryClient.setQueryData(queryKeys.generalMeetings.convocationByCode(meetingReference, code), confirmation),
  });
}
