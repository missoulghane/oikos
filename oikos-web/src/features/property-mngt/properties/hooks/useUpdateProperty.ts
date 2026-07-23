import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateProperty } from '@/features/property-mngt/properties/api/updateProperty';
import type { UpdatePropertyPayload } from '@/features/property-mngt/properties/types/property.types';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useUpdateProperty(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: UpdatePropertyPayload) => updateProperty(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.detail(propertyId) });
      queryClient.invalidateQueries({ queryKey: ['properties', 'list'] });
    },
  });
}
