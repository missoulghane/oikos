import { useQuery } from '@tanstack/react-query';
import { getInstallmentCallDetail } from '@/features/property-mngt/installments/api/getInstallmentCallDetail';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useInstallmentCallDetail(id: string, enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.installmentCalls.detail(id),
    queryFn: () => getInstallmentCallDetail(id),
    enabled,
  });
}
