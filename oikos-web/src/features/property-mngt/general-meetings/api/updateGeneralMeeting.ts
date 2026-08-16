import { httpClient } from '@/shared/api/httpClient';
import type {
  GeneralMeeting,
  UpdateGeneralMeetingPayload,
} from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

export async function updateGeneralMeeting(
  meetingId: string,
  payload: UpdateGeneralMeetingPayload,
): Promise<GeneralMeeting> {
  const { data } = await httpClient.put<GeneralMeeting>(`/general-meetings/${meetingId}`, payload);
  return data;
}
