import { useQuery } from '@tanstack/react-query';
import { getConvocation } from '@/features/property-mngt/general-meetings/api/getConvocation';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useConvocation(convocationId: string) {
  return useQuery({
    queryKey: queryKeys.generalMeetings.convocation(convocationId),
    queryFn: () => getConvocation(convocationId),
    enabled: convocationId !== '',
  });
}
