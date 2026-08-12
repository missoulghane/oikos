import { useQuery } from '@tanstack/react-query';
import { getMyPayments } from '@/features/property-ownership/payments/api/getMyPayments';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useMyPayments() {
  return useQuery({
    queryKey: queryKeys.me.payments(),
    queryFn: getMyPayments,
  });
}
