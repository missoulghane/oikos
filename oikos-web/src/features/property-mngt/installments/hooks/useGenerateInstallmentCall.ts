import { useMutation, useQueryClient } from '@tanstack/react-query';
import { generateInstallmentCall } from '@/features/property-mngt/installments/api/generateInstallmentCall';
import type { GenerateInstallmentCallPayload } from '@/features/property-mngt/installments/types/installmentCall.types';

export function useGenerateInstallmentCall(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: GenerateInstallmentCallPayload) => generateInstallmentCall(propertyId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'installment-calls'] });
    },
  });
}
