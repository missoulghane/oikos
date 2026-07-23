import { httpClient } from '@/shared/api/httpClient';
import type { Account, AccountType } from '@/features/property-mngt/accounting/types/accounting.types';

export async function getAccountByHolder(holderId: string, accountType: AccountType): Promise<Account> {
  const { data } = await httpClient.get<Account>('/accounts', { params: { holderId, accountType } });
  return data;
}
