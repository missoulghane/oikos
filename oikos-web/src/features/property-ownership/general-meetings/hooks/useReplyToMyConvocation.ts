import { useMutation, useQueryClient } from '@tanstack/react-query';
import { replyToConvocation } from '@/features/property-mngt/general-meetings/api/replyToConvocation';
import type { AttendanceReply } from '@/features/property-mngt/general-meetings/types/convocation.types';
import { queryKeys } from '@/shared/constants/queryKeys';

/**
 * Same endpoint as the syndic's (the API decides who may call it on which lot,
 * via ownsUnit) - reusing it rather than duplicating the call is what keeps
 * the two spaces from drifting apart.
 */
export function useReplyToMyConvocation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ convocationId, reply }: { convocationId: string; reply: AttendanceReply }) =>
      replyToConvocation(convocationId, reply),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.generalMeetings.myConvocations() }),
  });
}
