import { httpClient } from '@/shared/api/httpClient';
import type { GeneralMeeting } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

export async function getGeneralMeeting(meetingId: string): Promise<GeneralMeeting> {
  const { data } = await httpClient.get<GeneralMeeting>(`/general-meetings/${meetingId}`);
  return data;
}
