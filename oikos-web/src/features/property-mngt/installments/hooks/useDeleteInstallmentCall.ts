import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteInstallmentCall } from '@/features/property-mngt/installments/api/deleteInstallmentCall';

export function useDeleteInstallmentCall(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deleteInstallmentCall,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'installment-calls'] });
      // Deleting a call also deletes its échéances - refresh the Échéances tab too.
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'installments'] });
    },
  });
}
