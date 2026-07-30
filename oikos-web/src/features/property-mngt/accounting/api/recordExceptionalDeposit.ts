import { httpClient } from '@/shared/api/httpClient';
import type { RecordExceptionalDepositPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export async function recordExceptionalDeposit(
  propertyId: string,
  payload: RecordExceptionalDepositPayload,
): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/accounting/financial-accounts/deposits`, payload);
}
