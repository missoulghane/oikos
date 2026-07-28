import { useMutation, useQueryClient } from '@tanstack/react-query';
import { addUnitOwnership } from '@/features/property-mngt/properties/api/addUnitOwnership';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { AddUnitOwnershipPayload } from '@/features/property-mngt/properties/types/property.types';

export function useAddUnitOwnership(unitId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: AddUnitOwnershipPayload) => addUnitOwnership({ unitId, payload }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.units.owners(unitId) });
    },
  });
}
