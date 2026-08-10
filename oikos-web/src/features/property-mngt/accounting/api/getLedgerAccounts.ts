import { httpClient } from '@/shared/api/httpClient';
import type { LedgerAccount } from '@/features/property-mngt/accounting/types/accounting.types';

export async function getLedgerAccounts(propertyId: string): Promise<LedgerAccount[]> {
  const { data } = await httpClient.get<LedgerAccount[]>(`/properties/${propertyId}/accounting/ledger-accounts`);
  return data;
}
