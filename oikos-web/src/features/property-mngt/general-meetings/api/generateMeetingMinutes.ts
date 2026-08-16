import { httpClient } from '@/shared/api/httpClient';
import type { MeetingMinutes } from '@/features/property-mngt/general-meetings/types/minutes.types';

/** Re-running regenerates from the session's data and discards manual edits. */
export async function generateMeetingMinutes(meetingId: string): Promise<MeetingMinutes> {
  const { data } = await httpClient.post<MeetingMinutes>(`/general-meetings/${meetingId}/minutes`);
  return data;
}
