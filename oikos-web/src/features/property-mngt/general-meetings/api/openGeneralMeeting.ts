import { httpClient } from '@/shared/api/httpClient';
import type { GeneralMeeting } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

/**
 * forceWithoutQuorum is never sent implicitly: opening a session the quorum
 * does not carry is a deliberate act, recorded on the meeting and printed in
 * its minutes.
 */
export async function openGeneralMeeting(meetingId: string, forceWithoutQuorum: boolean): Promise<GeneralMeeting> {
  const { data } = await httpClient.post<GeneralMeeting>(`/general-meetings/${meetingId}/open`, {
    forceWithoutQuorum,
  });
  return data;
}
