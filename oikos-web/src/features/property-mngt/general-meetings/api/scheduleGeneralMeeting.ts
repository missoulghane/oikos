import { httpClient } from '@/shared/api/httpClient';
import type {
  GeneralMeeting,
  ScheduleGeneralMeetingPayload,
} from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

export async function scheduleGeneralMeeting(
  meetingId: string,
  payload: ScheduleGeneralMeetingPayload,
): Promise<GeneralMeeting> {
  const { data } = await httpClient.post<GeneralMeeting>(`/general-meetings/${meetingId}/schedule`, payload);
  return data;
}
