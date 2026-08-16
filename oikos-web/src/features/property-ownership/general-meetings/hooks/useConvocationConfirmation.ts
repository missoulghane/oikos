import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { confirmConvocation } from '@/features/property-ownership/general-meetings/api/confirmConvocation';
import { getConvocationConfirmation } from '@/features/property-ownership/general-meetings/api/getConvocationConfirmation';
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
