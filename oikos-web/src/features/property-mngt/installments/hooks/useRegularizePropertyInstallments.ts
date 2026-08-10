import { useMutation, useQueryClient } from '@tanstack/react-query';
import { regularizePropertyInstallments } from '@/features/property-mngt/installments/api/regularizePropertyInstallments';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useRegularizePropertyInstallments(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => regularizePropertyInstallments(propertyId),
    onSuccess: () => {
      // Prefix match: covers every cached page/filter combo of the property-wide list.
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'installments'] });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingLedgerAccounts(propertyId) });
    },
  });
}
