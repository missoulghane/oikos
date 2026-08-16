import { useMutation, useQueryClient } from '@tanstack/react-query';
import { addAgendaItem } from '@/features/property-mngt/general-meetings/api/addAgendaItem';
import { castVote } from '@/features/property-mngt/general-meetings/api/castVote';
import { checkInConvocation } from '@/features/property-mngt/general-meetings/api/checkInConvocation';
import { closeGeneralMeeting } from '@/features/property-mngt/general-meetings/api/closeGeneralMeeting';
import { closeVoteSession } from '@/features/property-mngt/general-meetings/api/closeVoteSession';
import { createGeneralMeeting } from '@/features/property-mngt/general-meetings/api/createGeneralMeeting';
import { deleteAgendaItem } from '@/features/property-mngt/general-meetings/api/deleteAgendaItem';
import { deleteGeneralMeeting } from '@/features/property-mngt/general-meetings/api/deleteGeneralMeeting';
import { generateConvocations } from '@/features/property-mngt/general-meetings/api/generateConvocations';
import { generateMeetingMinutes } from '@/features/property-mngt/general-meetings/api/generateMeetingMinutes';
import { openGeneralMeeting } from '@/features/property-mngt/general-meetings/api/openGeneralMeeting';
import { openVoteSession } from '@/features/property-mngt/general-meetings/api/openVoteSession';
import { publishMeetingMinutes } from '@/features/property-mngt/general-meetings/api/publishMeetingMinutes';
import { recordConvocationDelivery } from '@/features/property-mngt/general-meetings/api/recordConvocationDelivery';
import { recordShowOfHands } from '@/features/property-mngt/general-meetings/api/recordShowOfHands';
import { remindConvocations } from '@/features/property-mngt/general-meetings/api/remindConvocations';
import { sendPendingConvocations } from '@/features/property-mngt/general-meetings/api/sendPendingConvocations';
import { reorderAgendaItems } from '@/features/property-mngt/general-meetings/api/reorderAgendaItems';
import { replyToConvocation } from '@/features/property-mngt/general-meetings/api/replyToConvocation';
import { scheduleGeneralMeeting } from '@/features/property-mngt/general-meetings/api/scheduleGeneralMeeting';
import { sendConvocation } from '@/features/property-mngt/general-meetings/api/sendConvocation';
import { setQuorumSetting } from '@/features/property-mngt/general-meetings/api/setQuorumSetting';
import { undoCheckIn } from '@/features/property-mngt/general-meetings/api/undoCheckIn';
import { updateAgendaItem } from '@/features/property-mngt/general-meetings/api/updateAgendaItem';
import { updateGeneralMeeting } from '@/features/property-mngt/general-meetings/api/updateGeneralMeeting';
import { updateGeneralMeetingComment } from '@/features/property-mngt/general-meetings/api/updateGeneralMeetingComment';
import { updateMeetingMinutes } from '@/features/property-mngt/general-meetings/api/updateMeetingMinutes';
import { validateMeetingMinutes } from '@/features/property-mngt/general-meetings/api/validateMeetingMinutes';
import type {
  AgendaItemPayload,
  CreateGeneralMeetingPayload,
  MeetingType,
  ScheduleGeneralMeetingPayload,
  UpdateGeneralMeetingPayload,
} from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import type {
  AttendanceMode,
  AttendanceReply,
  ChannelCode,
  DeliveryStatus,
} from '@/features/property-mngt/general-meetings/types/convocation.types';
import type { ShowOfHandsPayload, VoteChoice } from '@/features/property-mngt/general-meetings/types/vote.types';
import { queryKeys } from '@/shared/constants/queryKeys';

/**
 * Every write of the module, grouped in one file rather than one per hook.
 *
 * <p>They are not independent: almost all of them shift the same meeting from
 * one state to the next, so what has to be invalidated afterwards is nearly
 * always "this meeting, and whatever hangs off it". Spreading twenty
 * near-identical invalidation blocks over twenty files is how one of them ends
 * up forgotten - and a stale cache here shows a syndic a ballot that is
 * already closed.
 */
function useMeetingScopedInvalidation(meetingId: string) {
  const queryClient = useQueryClient();
  return () => {
    queryClient.invalidateQueries({ queryKey: ['general-meetings', meetingId] });
    // The list carries the status badge and the agenda count, both of which
    // most of these mutations move.
    queryClient.invalidateQueries({ queryKey: ['general-meetings'] });
  };
}

export function useCreateGeneralMeeting(propertyId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateGeneralMeetingPayload) => createGeneralMeeting(propertyId, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['general-meetings', propertyId] }),
  });
}

export function useUpdateGeneralMeeting(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({
    mutationFn: (payload: UpdateGeneralMeetingPayload) => updateGeneralMeeting(meetingId, payload),
    onSuccess: invalidate,
  });
}

export function useScheduleGeneralMeeting(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({
    mutationFn: (payload: ScheduleGeneralMeetingPayload) => scheduleGeneralMeeting(meetingId, payload),
    onSuccess: invalidate,
  });
}

export function useDeleteGeneralMeeting(propertyId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (meetingId: string) => deleteGeneralMeeting(meetingId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['general-meetings', propertyId] }),
  });
}

export function useOpenGeneralMeeting(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({
    mutationFn: (forceWithoutQuorum: boolean) => openGeneralMeeting(meetingId, forceWithoutQuorum),
    onSuccess: invalidate,
  });
}

export function useCloseGeneralMeeting(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({ mutationFn: () => closeGeneralMeeting(meetingId), onSuccess: invalidate });
}

export function useAddAgendaItem(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({
    mutationFn: (payload: AgendaItemPayload) => addAgendaItem(meetingId, payload),
    onSuccess: invalidate,
  });
}

export function useUpdateAgendaItem(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({
    mutationFn: ({ agendaItemId, payload }: { agendaItemId: string; payload: AgendaItemPayload }) =>
      updateAgendaItem(agendaItemId, payload),
    onSuccess: invalidate,
  });
}

export function useDeleteAgendaItem(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({ mutationFn: (agendaItemId: string) => deleteAgendaItem(agendaItemId), onSuccess: invalidate });
}

export function useReorderAgendaItems(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({
    mutationFn: (orderedItemIds: string[]) => reorderAgendaItems(meetingId, orderedItemIds),
    onSuccess: invalidate,
  });
}

export function useUpdateGeneralMeetingComment(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({
    mutationFn: (comment: string) => updateGeneralMeetingComment(meetingId, comment),
    onSuccess: invalidate,
  });
}

export function useGenerateConvocations(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({ mutationFn: () => generateConvocations(meetingId), onSuccess: invalidate });
}

export function useSendPendingConvocations(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({
    mutationFn: (channel: ChannelCode) => sendPendingConvocations(meetingId, channel),
    onSuccess: invalidate,
  });
}

export function useRemindConvocations(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({ mutationFn: () => remindConvocations(meetingId), onSuccess: invalidate });
}

export function useSendConvocation(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({
    mutationFn: ({ convocationId, channel }: { convocationId: string; channel: ChannelCode }) =>
      sendConvocation(convocationId, channel),
    onSuccess: invalidate,
  });
}

export function useRecordConvocationDelivery(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({
    mutationFn: (input: {
      convocationId: string;
      channel: ChannelCode;
      deliveryStatus: DeliveryStatus;
      reference?: string;
    }) => recordConvocationDelivery(input.convocationId, input.channel, input.deliveryStatus, input.reference),
    onSuccess: invalidate,
  });
}

export function useReplyToConvocation(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({
    mutationFn: ({
      convocationId,
      reply,
      note,
    }: {
      convocationId: string;
      reply: AttendanceReply;
      note?: string;
    }) => replyToConvocation(convocationId, reply, note),
    onSuccess: invalidate,
  });
}

export function useCheckInConvocation(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({
    mutationFn: ({ convocationId, mode }: { convocationId: string; mode: AttendanceMode }) =>
      checkInConvocation(convocationId, mode),
    onSuccess: invalidate,
  });
}

export function useUndoCheckIn(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({ mutationFn: (convocationId: string) => undoCheckIn(convocationId), onSuccess: invalidate });
}

export function useOpenVoteSession(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (agendaItemId: string) => openVoteSession(agendaItemId),
    onSuccess: (_data, agendaItemId) => {
      invalidate();
      queryClient.invalidateQueries({ queryKey: ['agenda-items', agendaItemId] });
    },
  });
}

export function useCloseVoteSession(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (agendaItemId: string) => closeVoteSession(agendaItemId),
    onSuccess: (_data, agendaItemId) => {
      invalidate();
      queryClient.invalidateQueries({ queryKey: ['agenda-items', agendaItemId] });
    },
  });
}

export function useCastVote() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ agendaItemId, unitId, choice }: { agendaItemId: string; unitId: string; choice: VoteChoice }) =>
      castVote(agendaItemId, unitId, choice),
    onSuccess: (_data, { agendaItemId }) =>
      queryClient.invalidateQueries({ queryKey: ['agenda-items', agendaItemId] }),
  });
}

export function useRecordShowOfHands() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ agendaItemId, payload }: { agendaItemId: string; payload: ShowOfHandsPayload }) =>
      recordShowOfHands(agendaItemId, payload),
    onSuccess: (_data, { agendaItemId }) =>
      queryClient.invalidateQueries({ queryKey: ['agenda-items', agendaItemId] }),
  });
}

export function useGenerateMeetingMinutes(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({ mutationFn: () => generateMeetingMinutes(meetingId), onSuccess: invalidate });
}

export function useUpdateMeetingMinutes(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({ mutationFn: (content: string) => updateMeetingMinutes(meetingId, content), onSuccess: invalidate });
}

export function useValidateMeetingMinutes(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({ mutationFn: () => validateMeetingMinutes(meetingId), onSuccess: invalidate });
}

export function usePublishMeetingMinutes(meetingId: string) {
  const invalidate = useMeetingScopedInvalidation(meetingId);
  return useMutation({ mutationFn: () => publishMeetingMinutes(meetingId), onSuccess: invalidate });
}

export function useSetQuorumSetting(propertyId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ meetingType, quorumPercentage }: { meetingType: MeetingType; quorumPercentage: number }) =>
      setQuorumSetting(propertyId, meetingType, quorumPercentage),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: queryKeys.generalMeetings.quorumSettings(propertyId) }),
  });
}
