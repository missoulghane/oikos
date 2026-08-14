import { httpClient } from '@/shared/api/httpClient';
import type { Installment } from '@/features/property-ownership/installments/types/installment.types';

/**
 * Despite the server-side guard being named `managesInstallment`, it resolves to
 * `managesUnit || ownsUnit`, so an owner may read their own echeance here. This
 * is the only endpoint exposing `period`, which GET /users/me/installments drops.
 */
export async function getInstallment(installmentId: string): Promise<Installment> {
  const { data } = await httpClient.get<Installment>(`/installments/${installmentId}`);
  return data;
}
