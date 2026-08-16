import { useQuery } from '@tanstack/react-query';
import { listGeneralMeetings } from '@/features/property-mngt/general-meetings/api/listGeneralMeetings';
import type { GeneralMeetingFilters } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useGeneralMeetings(
  propertyId: string,
  page: number,
  size: number,
  filters: GeneralMeetingFilters,
) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.list(propertyId, page, size, filters),
    queryFn: () => listGeneralMeetings({ propertyId, page, size, ...filters }),
    placeholderData: (previousData) => previousData,
  });
}
