import { useMutation, useQueryClient } from '@tanstack/react-query';
import { addUnitTypeDefinition } from '@/features/property-mngt/properties/api/addUnitTypeDefinition';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useAddUnitTypeDefinition(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (name: string) => addUnitTypeDefinition({ propertyId, name }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.unitTypes(propertyId) });
    },
  });
}
