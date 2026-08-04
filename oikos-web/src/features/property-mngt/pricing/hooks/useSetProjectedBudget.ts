import { useMutation, useQueryClient } from '@tanstack/react-query';
import { setProjectedBudget } from '@/features/property-mngt/pricing/api/setProjectedBudget';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useSetProjectedBudget(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (projectedBudget: number) => setProjectedBudget(propertyId, projectedBudget),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.detail(propertyId) });
    },
  });
}
