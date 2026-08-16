import { httpClient } from '@/shared/api/httpClient';
import type { MeetingMinutes } from '@/features/property-mngt/general-meetings/types/minutes.types';

export async function updateMeetingMinutes(meetingId: string, content: string): Promise<MeetingMinutes> {
  const { data } = await httpClient.put<MeetingMinutes>(`/general-meetings/${meetingId}/minutes`, { content });
  return data;
}
