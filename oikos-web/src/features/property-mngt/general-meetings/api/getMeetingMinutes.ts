import { httpClient } from '@/shared/api/httpClient';
import type { MeetingMinutes } from '@/features/property-mngt/general-meetings/types/minutes.types';

export async function getMeetingMinutes(meetingId: string): Promise<MeetingMinutes> {
  const { data } = await httpClient.get<MeetingMinutes>(`/general-meetings/${meetingId}/minutes`);
  return data;
}
