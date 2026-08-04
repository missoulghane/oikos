import { useQuery } from '@tanstack/react-query';
import { getMyInstallments } from '@/features/property-ownership/installments/api/getMyInstallments';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useMyInstallments() {
  return useQuery({
    queryKey: queryKeys.me.installments(),
    queryFn: getMyInstallments,
  });
}
