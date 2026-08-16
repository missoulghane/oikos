import { httpClient } from '@/shared/api/httpClient';
import type { CreateGeneralMeetingPayload } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

export async function createGeneralMeeting(
  propertyId: string,
  payload: CreateGeneralMeetingPayload,
): Promise<{ id: string }> {
  const { data } = await httpClient.post<{ id: string }>(`/properties/${propertyId}/general-meetings`, payload);
  return data;
}
