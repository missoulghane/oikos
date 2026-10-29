import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cancelInstallmentCall } from '@/features/property-mngt/installments/api/cancelInstallmentCall';

export function useCancelInstallmentCall(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: cancelInstallmentCall,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'installment-calls'] });
      // Cancelling a call removes its échéances - refresh the Échéances tab too.
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'installments'] });
      // The contre-passation is a new journal entry: balances, ledger and journal move.
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'accounting'] });
    },
  });
}
