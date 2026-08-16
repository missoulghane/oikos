import { httpClient } from '@/shared/api/httpClient';
import type { MeetingMinutes } from '@/features/property-mngt/general-meetings/types/minutes.types';

/** Freezes the text; nothing but publication may follow. */
export async function validateMeetingMinutes(meetingId: string): Promise<MeetingMinutes> {
  const { data } = await httpClient.post<MeetingMinutes>(`/general-meetings/${meetingId}/minutes/validate`);
  return data;
}
