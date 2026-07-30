import { useMutation, useQueryClient } from '@tanstack/react-query';
import { recordUnitAccountRegularization } from '@/features/property-mngt/accounting/api/recordUnitAccountRegularization';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { RecordUnitAccountRegularizationPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export function useRecordUnitAccountRegularization(propertyId: string, unitId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: RecordUnitAccountRegularizationPayload) =>
      recordUnitAccountRegularization(propertyId, unitId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.units.account(unitId) });
      // Prefix match (no page/size) invalidates every loaded page of movements.
      queryClient.invalidateQueries({ queryKey: ['units', unitId, 'account', 'movements'] });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingUnitsSummary(propertyId) });
    },
  });
}
