import { useMutation, useQueryClient } from '@tanstack/react-query';
import { validateUnitLettrage } from '@/features/property-mngt/accounting/api/validateUnitLettrage';
import { queryKeys } from '@/shared/constants/queryKeys';

export function useValidateUnitLettrage(propertyId: string, unitId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => validateUnitLettrage(propertyId, unitId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.units.account(unitId) });
      // Prefix match (no page/size) invalidates every loaded page of movements.
      queryClient.invalidateQueries({ queryKey: ['units', unitId, 'account', 'movements'] });
      queryClient.invalidateQueries({ queryKey: queryKeys.units.lettrageProposal(unitId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingUnitsSummary(propertyId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingPendingLettrages(propertyId) });
    },
  });
}
