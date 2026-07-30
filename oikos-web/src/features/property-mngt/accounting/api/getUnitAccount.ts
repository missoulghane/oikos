import { httpClient } from '@/shared/api/httpClient';
import type { UnitAccount } from '@/features/property-mngt/accounting/types/accounting.types';

export async function getUnitAccount(unitId: string): Promise<UnitAccount> {
  const { data } = await httpClient.get<UnitAccount>(`/units/${unitId}/account`);
  return data;
}
