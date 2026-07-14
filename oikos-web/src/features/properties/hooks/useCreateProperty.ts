import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createProperty } from '@/features/properties/api/createProperty';

export function useCreateProperty() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createProperty,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['properties', 'list'] });
    },
  });
}
