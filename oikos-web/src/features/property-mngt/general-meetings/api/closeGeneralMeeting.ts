import { httpClient } from '@/shared/api/httpClient';
import type { GeneralMeeting } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

export async function closeGeneralMeeting(meetingId: string): Promise<GeneralMeeting> {
  const { data } = await httpClient.post<GeneralMeeting>(`/general-meetings/${meetingId}/close`);
  return data;
}
