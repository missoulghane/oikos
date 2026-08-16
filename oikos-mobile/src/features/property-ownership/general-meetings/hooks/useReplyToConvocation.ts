import { useMutation, useQueryClient } from '@tanstack/react-query';
import { replyToConvocation } from '@/features/property-ownership/general-meetings/api/replyToConvocation';
import type { AttendanceReply } from '@/features/property-ownership/general-meetings/types/generalMeeting.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useReplyToConvocation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ convocationId, reply }: { convocationId: string; reply: AttendanceReply }) =>
      replyToConvocation(convocationId, reply),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.me.convocations() }),
  });
}
