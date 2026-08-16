import { useQuery } from '@tanstack/react-query';
import { listMyConvocations } from '@/features/property-ownership/general-meetings/api/listMyConvocations';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useMyConvocations() {
  return useQuery({
    queryKey: queryKeys.generalMeetings.myConvocations(),
    queryFn: listMyConvocations,
  });
}
