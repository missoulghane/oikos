import { httpClient } from '@/shared/api/httpClient';
import type { MeetingQuorumSetting } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

/** Only the meeting types actually configured are returned - an absent one was never decided. */
export async function listQuorumSettings(propertyId: string): Promise<MeetingQuorumSetting[]> {
  const { data } = await httpClient.get<MeetingQuorumSetting[]>(`/properties/${propertyId}/meeting-quorum-settings`);
  return data;
}
