import { httpClient } from '@/shared/api/httpClient';
import type {
  GeneralMeetingFilters,
  PagedGeneralMeetings,
} from '@/features/property-mngt/general-meetings/types/generalMeeting.types';

export interface ListGeneralMeetingsParams extends GeneralMeetingFilters {
  propertyId: string;
  page: number;
  size: number;
}

export async function listGeneralMeetings({
  propertyId,
  page,
  size,
  status,
  type,
}: ListGeneralMeetingsParams): Promise<PagedGeneralMeetings> {
  const { data } = await httpClient.get<PagedGeneralMeetings>(`/properties/${propertyId}/general-meetings`, {
    params: { page, size, status, type },
  });
  return data;
}
