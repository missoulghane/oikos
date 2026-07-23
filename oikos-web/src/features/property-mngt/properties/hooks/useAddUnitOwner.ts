import { useMutation, useQueryClient } from '@tanstack/react-query';
import { addUnitOwner } from '@/features/property-mngt/properties/api/addUnitOwner';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { AddUnitOwnerPayload } from '@/features/property-mngt/properties/types/property.types';

export function useAddUnitOwner(unitId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: AddUnitOwnerPayload) => addUnitOwner({ unitId, payload }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.units.owners(unitId) });
    },
  });
}
