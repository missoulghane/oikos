import { useMutation } from '@tanstack/react-query';
import { addPropertyManager } from '@/features/property-mngt/properties/api/addPropertyManager';

export function useAddPropertyManager(propertyId: string) {
  return useMutation({
    mutationFn: (email: string) => addPropertyManager({ propertyId, email }),
  });
}
