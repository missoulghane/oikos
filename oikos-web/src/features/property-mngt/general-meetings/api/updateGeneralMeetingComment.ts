import { httpClient } from '@/shared/api/httpClient';
import type { GeneralMeeting } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

/**
 * Its own endpoint rather than a field on updateGeneralMeeting: saving a
 * comment must not be able to carry a stale date and venue along with it.
 */
export async function updateGeneralMeetingComment(meetingId: string, comment: string): Promise<GeneralMeeting> {
  const { data } = await httpClient.put<GeneralMeeting>(`/general-meetings/${meetingId}/comment`, { comment });
  return data;
}
