import { httpClient } from '@/shared/api/httpClient';
import type { Installment } from '@/features/property-mngt/installments/types/installment.types';

/**
 * The single echeance, for both spaces: the server-side guard is named
 * `managesInstallment` but resolves to `managesUnit || ownsUnit`, so the syndic
 * detail page and the owner's own read the very same endpoint. It is also the
 * only one exposing `period`, which the owner list endpoint
 * (GET /users/me/installments) drops - hence the richer `Installment` type.
 */
export async function getInstallment(installmentId: string): Promise<Installment> {
  const { data } = await httpClient.get<Installment>(`/installments/${installmentId}`);
  return data;
}
