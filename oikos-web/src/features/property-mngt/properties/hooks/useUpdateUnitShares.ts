import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateUnitShares } from '@/features/property-mngt/properties/api/updateUnitShares';
import type { UpdateUnitSharesPayload } from '@/features/property-mngt/properties/types/property.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUpdateUnitShares(unitId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: UpdateUnitSharesPayload) => updateUnitShares(unitId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.units.detail(unitId) });
    },
  });
}
