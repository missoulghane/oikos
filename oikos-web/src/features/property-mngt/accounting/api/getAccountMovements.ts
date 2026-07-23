import { httpClient } from '@/shared/api/httpClient';
import type { PagedMovements } from '@/features/property-mngt/accounting/types/accounting.types';

export interface GetAccountMovementsParams {
  accountId: string;
  page: number;
  size: number;
}

export async function getAccountMovements({ accountId, page, size }: GetAccountMovementsParams): Promise<PagedMovements> {
  const { data } = await httpClient.get<PagedMovements>(`/accounts/${accountId}/movements`, {
    params: { page, size },
  });
  return data;
}
