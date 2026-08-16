import { httpClient } from '@/shared/api/httpClient';
import type {
  MeetingQuorumSetting,
  MeetingType,
} from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

export async function setQuorumSetting(
  propertyId: string,
  meetingType: MeetingType,
  quorumPercentage: number,
): Promise<MeetingQuorumSetting> {
  const { data } = await httpClient.put<MeetingQuorumSetting>(`/properties/${propertyId}/meeting-quorum-settings`, {
    meetingType,
    quorumPercentage,
  });
  return data;
}
