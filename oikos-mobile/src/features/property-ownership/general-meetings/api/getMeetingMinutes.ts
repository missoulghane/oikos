import { httpClient } from '@/shared/api/httpClient';
import type { MeetingMinutes } from '@/features/property-ownership/general-meetings/types/generalMeeting.types';

export async function getMeetingMinutes(meetingId: string): Promise<MeetingMinutes> {
  const { data } = await httpClient.get<MeetingMinutes>(`/general-meetings/${meetingId}/minutes`);
  return data;
}
