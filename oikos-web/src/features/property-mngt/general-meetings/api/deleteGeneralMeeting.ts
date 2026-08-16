import { httpClient } from '@/shared/api/httpClient';

export async function deleteGeneralMeeting(meetingId: string): Promise<void> {
  await httpClient.delete(`/general-meetings/${meetingId}`);
}
