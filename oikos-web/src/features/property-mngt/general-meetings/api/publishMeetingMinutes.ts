import { httpClient } from '@/shared/api/httpClient';
import type { MeetingMinutes } from '@/features/property-mngt/general-meetings/types/minutes.types';

export async function publishMeetingMinutes(meetingId: string): Promise<MeetingMinutes> {
  const { data } = await httpClient.post<MeetingMinutes>(`/general-meetings/${meetingId}/minutes/publish`);
  return data;
}
