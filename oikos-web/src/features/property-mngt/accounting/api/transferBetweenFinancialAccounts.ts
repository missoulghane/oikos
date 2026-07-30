import { httpClient } from '@/shared/api/httpClient';
import type { TransferBetweenFinancialAccountsPayload } from '@/features/property-mngt/accounting/types/accounting.types';

export async function transferBetweenFinancialAccounts(
  propertyId: string,
  payload: TransferBetweenFinancialAccountsPayload,
): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/accounting/financial-accounts/transfers`, payload);
}
