import { useQuery } from '@tanstack/react-query';
import { getInstallment } from '@/features/property-mngt/installments/api/getInstallment';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useInstallment(installmentId: string) {
  return useQuery({
    queryKey: queryKeys.installments.detail(installmentId),
    queryFn: () => getInstallment(installmentId),
    enabled: Boolean(installmentId),
  });
}
