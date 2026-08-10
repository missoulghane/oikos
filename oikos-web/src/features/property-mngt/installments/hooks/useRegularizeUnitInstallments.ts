import { useMutation, useQueryClient } from '@tanstack/react-query';
import { regularizeUnitInstallments } from '@/features/property-mngt/installments/api/regularizeUnitInstallments';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useRegularizeUnitInstallments(propertyId: string, unitId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => regularizeUnitInstallments(propertyId, unitId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.units.installments(unitId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.units.payments(unitId) });
      // Prefix match: covers every cached page/filter combo of the property-wide list.
      queryClient.invalidateQueries({ queryKey: ['properties', propertyId, 'installments'] });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingLedgerAccounts(propertyId) });
    },
  });
}
